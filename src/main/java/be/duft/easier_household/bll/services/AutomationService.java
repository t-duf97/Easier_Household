package be.duft.easier_household.bll.services;

import be.duft.easier_household.dl.entities.*;
import be.duft.easier_household.dl.enums.Role;
import be.duft.easier_household.api.dtos.request.AutomationActionRequest;
import be.duft.easier_household.api.dtos.request.AutomationRequest;
import be.duft.easier_household.api.dtos.response.AutomationResponse;
import be.duft.easier_household.bll.exceptions.ResourceNotFoundException;
import be.duft.easier_household.bll.exceptions.UnauthorizedAccessException;
import be.duft.easier_household.bll.mappers.AutomationActionMapper;
import be.duft.easier_household.bll.mappers.AutomationConditionMapper;
import be.duft.easier_household.bll.mappers.AutomationMapper;
import be.duft.easier_household.bll.mappers.AutomationTriggerMapper;
import be.duft.easier_household.dl.repositories.AutomationRepository;
import be.duft.easier_household.dl.repositories.DeviceRepository;
import be.duft.easier_household.dl.repositories.HouseholdMemberRepository;
import be.duft.easier_household.dl.repositories.HouseholdRepository;
import be.duft.easier_household.dl.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AutomationService {

    private final AutomationRepository automationRepository;
    private final HouseholdRepository householdRepository;
    private final HouseholdMemberRepository householdMemberRepository;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final AutomationMapper automationMapper;

    private final AutomationTriggerMapper automationTriggerMapper;
    private final AutomationConditionMapper automationConditionMapper;
    private final AutomationActionMapper automationActionMapper;

    public AutomationResponse create(Long householdId, AutomationRequest request, Long userId){
        Household household = householdRepository.findById(householdId)
                .orElseThrow(()-> new ResourceNotFoundException("Household not found: " +  householdId));

        householdMemberRepository.findByHouseholdIdAndUserId(householdId, userId)
                .orElseThrow(()-> new UnauthorizedAccessException("User is not a member of this household"));

        User user = userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        if (request.getTriggers() == null || request.getTriggers().isEmpty()) {
            throw new IllegalArgumentException("At least one trigger is required");
        }
        if (request.getActions() == null || request.getActions().isEmpty()) {
            throw new IllegalArgumentException("At least one action is required");
        }

        Automation automation = automationMapper.fromRequestToEntity(request, household, user);
        replaceChildren(automation, request);
        return automationMapper.fromEntityToResponse(automationRepository.save(automation));
    }

    @Transactional(readOnly = true)
    public List<AutomationResponse> findByHousehold(Long householdId, Long userId) {
        householdMemberRepository.findByHouseholdIdAndUserId(householdId, userId)
                .orElseThrow(()-> new UnauthorizedAccessException("User is not a member of this household"));

        return automationRepository.findByHouseholdId(householdId).stream()
                .map(automationMapper::fromEntityToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AutomationResponse findById(Long id, Long userId) {
        Automation automation = getAutomationOrThrow(id);
        checkMembership(automation.getHousehold().getId(), userId);
        return automationMapper.fromEntityToResponse(automation);
    }

    public AutomationResponse toggleEnabled(Long id, Long userId) {
        Automation automation = getAutomationOrThrow(id);
        checkMembership(automation.getHousehold().getId(), userId);
        automation.setEnabled(!automation.isEnabled());
        return automationMapper.fromEntityToResponse(automationRepository.save(automation));
    }

    public AutomationResponse update(Long id, AutomationRequest request, Long userId) {
        Automation automation = getAutomationOrThrow(id);
        checkMembership(automation.getHousehold().getId(), userId);

        automation.setName(request.getName());
        automation.setDescription(request.getDescription());
        automation.setLogic(request.getLogic() != null ? request.getLogic() : automation.getLogic());

        replaceChildren(automation, request);
        return automationMapper.fromEntityToResponse(automationRepository.save(automation));
    }

    /**
     * Remplace triggers, conditions et actions de l'automatisation par ceux de la requête.
     * Chaque device référencé doit appartenir au foyer de l'automatisation.
     */
    private void replaceChildren(Automation automation, AutomationRequest request) {
        Long householdId = automation.getHousehold().getId();

        automation.getTriggers().clear();
        automation.getConditions().clear();
        automation.getActions().clear();

        if (request.getTriggers() != null) {
            request.getTriggers().forEach(t -> {
                AutomationTrigger trigger = automationTriggerMapper.fromRequestToEntity(
                        t, resolveDevice(t.getDeviceId(), householdId));
                trigger.setAutomation(automation);
                automation.getTriggers().add(trigger);
            });
        }
        if (request.getConditions() != null) {
            request.getConditions().forEach(c -> {
                AutomationCondition condition = automationConditionMapper.fromRequestToEntity(
                        c, resolveDevice(c.getDeviceId(), householdId));
                condition.setAutomation(automation);
                automation.getConditions().add(condition);
            });
        }
        if (request.getActions() != null) {
            /**
             * Cette boucle me permet d'attribuer un ordre si aucun ordre n'est renseigné pour une action
             * En effet aucun ordre renseigné -> 0 / 0 devient la position +1
             * **/
            for (int i = 0; i < request.getActions().size(); i++) {
                AutomationActionRequest a = request.getActions().get(i);
                AutomationAction action = automationActionMapper.fromRequestToEntity(
                        a, resolveDevice(a.getDeviceId(), householdId));
                if (action.getExecutionOrder() == 0) {
                    action.setExecutionOrder(i + 1);
                }
                action.setAutomation(automation);
                automation.getActions().add(action);
            }
        }
    }

    /**
     * @return le device, ou null si {@code deviceId} est null
     * @throws ResourceNotFoundException si le device n'existe pas ou n'appartient pas au foyer
     *         (même réponse dans les deux cas, pour ne pas révéler les devices des autres foyers)
     */
    private Device resolveDevice(Long deviceId, Long householdId) {
        if (deviceId == null) {
            return null;
        }
        return deviceRepository.findById(deviceId)
                .filter(device -> device.getAssignedRoom() != null
                        && householdId.equals(device.getAssignedRoom().getHousehold().getId()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Device " + deviceId + " not found in household " + householdId));
    }


    public void delete(Long id, Long userId) {
        Automation automation = getAutomationOrThrow(id);
        checkMembership(automation.getHousehold().getId(), userId);
        automationRepository.delete(automation);
    }



    public Automation getAutomationOrThrow(Long id) {
        return automationRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Automation not found: " +  id));
    }

    private void checkMembership(Long householdId, Long userId) {
        HouseholdMember member = householdMemberRepository
                .findByHouseholdIdAndUserId(householdId, userId)
                .orElseThrow(()-> new UnauthorizedAccessException("User is not a member of this household"));
    }

    private void checkOwnership(Long householdId, Long userId) {
        HouseholdMember member = householdMemberRepository
                .findByHouseholdIdAndUserId(householdId, userId)
                .orElseThrow(()-> new UnauthorizedAccessException("User is not a member of this household"));
        if (member.getRole() != Role.OWNER) {
            throw new UnauthorizedAccessException("Only the owner can perform this operation");
        }
    }
}
