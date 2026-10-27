package be.duft.easier_household.bll.mappers;

import be.duft.easier_household.dl.entities.*;
import be.duft.easier_household.dl.enums.ConditionLogic;
import be.duft.easier_household.api.dtos.request.AutomationRequest;
import be.duft.easier_household.api.dtos.response.AutomationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AutomationMapper {

    private final AutomationConditionMapper automationConditionMapper;
    private final AutomationTriggerMapper automationTriggerMapper;
    private final AutomationActionMapper automationActionMapper;

    public AutomationResponse fromEntityToResponse (Automation automation) {
        AutomationResponse response = new AutomationResponse();

        response.setId(automation.getId());
        response.setName(automation.getName());
        response.setDescription(automation.getDescription());
        response.setEnabled(automation.isEnabled());
        response.setLogic(automation.getLogic());
        response.setLastTriggeredAt(automation.getLastTriggeredAt());
        response.setCreatedAt(automation.getCreatedAt());
        response.setHouseholdId(automation.getHousehold().getId());

        response.setTriggers(automation.getTriggers().stream()
                .map(automationTriggerMapper::fromEntityToResponse)
                .collect(Collectors.toList()));

        response.setConditions(automation.getConditions().stream()
                .map(automationConditionMapper::fromEntityToResponse)
                .collect(Collectors.toList()));

        response.setActions(automation.getActions().stream()
                .map(automationActionMapper::fromEntityToResponse)
                .collect(Collectors.toList()));

        return response;
    }

    public Automation fromRequestToEntity(AutomationRequest request, Household household, User createdBy) {
        Automation automation = new Automation();

        automation.setName(request.getName());
        automation.setDescription(request.getDescription());
        automation.setEnabled(true);
        automation.setLogic(request.getLogic() != null
                ? request.getLogic() : ConditionLogic.ALL);
        automation.setHousehold(household);
        automation.setCreatedBy(createdBy);
        automation.setCreatedAt(LocalDateTime.now());

        if (request.getTriggers() != null) {
            List<AutomationTrigger> triggers = request.getTriggers().stream()
                    .map(automationTriggerMapper::fromRequestToEntity)
                    .collect(Collectors.toList());
            triggers.forEach(t -> t.setAutomation(automation));
            automation.setTriggers(triggers);
        }

        if (request.getConditions() != null) {
            List<AutomationCondition> conditions = request.getConditions().stream()
                    .map(automationConditionMapper::fromRequestToEntity)
                    .collect(Collectors.toList());
            conditions.forEach(c -> c.setAutomation(automation));
            automation.setConditions(conditions);
        }

        if (request.getActions() != null) {
            List<AutomationAction> actions = request.getActions().stream()
                    .map(automationActionMapper::fromRequestToEntity)
                    .collect(Collectors.toList());

            /**
             * Cette boucle me permet d'attribuer un ordre si aucun ordre n'est renseigné pour une action
             * En effet aucun ordre renseigné -> 0 / 0 devient la position +1
             * **/
            for (int i = 0; i < actions.size(); i++) {
                if (actions.get(i).getExecutionOrder() == 0) {
                    actions.get(i).setExecutionOrder(i + 1);
                }
                actions.get(i).setAutomation(automation);
            }
            automation.setActions(actions);
        }

        return automation;
    }

}
