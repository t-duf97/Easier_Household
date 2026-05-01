package be.duft.easier_household.dtos.requestDTO;

import be.duft.easier_household.dl.enums.ActionType;
import be.duft.easier_household.dl.enums.DeviceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AutomationActionRequest {

    private int executionOrder;

    @NotNull
    private ActionType actionType;

    @NotNull
    private DeviceStatus target_status;

    private String targetValue;

    @NotBlank
    private String notificationMessage;

    private int delayInSeconds;


}
