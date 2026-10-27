package be.duft.easier_household.api.dtos.request;

import be.duft.easier_household.dl.enums.ConditionLogic;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class AutomationRequest {

    @NotBlank(message = "Name is required!")
    private String name;

    private String description;

    private ConditionLogic logic;

    @NotEmpty(message = "An automation must depend on at least one trigger.")
    private List<AutomationTriggerRequest> triggers;

    @NotEmpty(message= "An automation must consist of at least one action.")
    private List<AutomationActionRequest> actions;

    private List<AutomationConditionRequest> conditions;


}
