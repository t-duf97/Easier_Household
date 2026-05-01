package be.duft.easier_household.dtos.requestDTO;

import be.duft.easier_household.dl.enums.ConditionLogic;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AutomationRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    private ConditionLogic conditionLogic;

    @NotNull
    private boolean isEnabled;


}
