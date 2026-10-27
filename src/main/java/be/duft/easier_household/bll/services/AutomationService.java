package be.duft.easier_household.bll.services;

import be.duft.easier_household.dl.entities.*;
import be.duft.easier_household.dl.enums.Role;
import be.duft.easier_household.api.dtos.request.AutomationRequest;
import be.duft.easier_household.api.dtos.response.AutomationResponse;
import be.duft.easier_household.bll.exceptions.ResourceNotFoundException;
import be.duft.easier_household.bll.exceptions.UnauthorizedAccessException;
import be.duft.easier_household.bll.mappers.AutomationActionMapper;
import be.duft.easier_household.bll.mappers.AutomationConditionMapper;
import be.duft.easier_household.bll.mappers.AutomationMapper;
import be.duft.easier_household.bll.mappers.AutomationTriggerMapper;
import be.duft.easier_household.dl.repositories.AutomationRepository;
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

        automation.getTriggers().clear();
        automation.getConditions().clear();
        automation.getActions().clear();

        if (request.getTriggers() != null) {
            request.getTriggers().forEach(t -> {
                AutomationTrigger trigger = automationTriggerMapper.fromRequestToEntity(t);
                trigger.setAutomation(automation);
                automation.getTriggers().add(trigger);
            });
        }
        if (request.getConditions() != null) {
            request.getConditions().forEach(c -> {
                AutomationCondition condition = automationConditionMapper.fromRequestToEntity(c);
                condition.setAutomation(automation);
                automation.getConditions().add(condition);
            });
        }
        if (request.getActions() != null) {
            request.getActions().forEach(a -> {
                AutomationAction action = automationActionMapper.fromRequestToEntity(a);
                action.setAutomation(automation);
                automation.getActions().add(action);
            });
        }

        return automationMapper.fromEntityToResponse(automationRepository.save(automation));
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
