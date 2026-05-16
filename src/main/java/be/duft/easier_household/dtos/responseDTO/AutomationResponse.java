package be.duft.easier_household.dtos.responseDTO;

import be.duft.easier_household.dl.enums.ConditionLogic;
import lombok.Setter;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class AutomationResponse {

    private Long id;
    private Long householdId;
    private String name;
    private String description;
    private ConditionLogic conditionLogic;
    private boolean isEnabled;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastTriggeredAt;

    private List<AutomationActionResponse> actions;
    private List<AutomationTriggerResponse> triggers;
    private List<AutomationConditionResponse> conditions;
}
