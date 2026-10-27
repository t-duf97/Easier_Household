package be.duft.easier_household.api.dtos.request;

import be.duft.easier_household.dl.enums.ActionType;
import be.duft.easier_household.dl.enums.DeviceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AutomationActionRequest {

    private Integer executionOrder;

    @NotNull
    private ActionType type;


    private DeviceStatus status;

    private String targetValue;


    private String notificationMsg;

    private Integer delayInSeconds;

    private Long deviceId;
}
