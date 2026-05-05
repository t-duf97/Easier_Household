package be.duft.easier_household.dtos.responseDTO;

import be.duft.easier_household.dl.enums.ActionType;
import be.duft.easier_household.dl.enums.DeviceStatus;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AutomationActionResponse {

    private Long id;
    private Long automationId;
    private Long deviceId;
    private Long executionOrder;
    private ActionType actionType;
    private DeviceStatus deviceStatus;
    private String targetValue;
    private String notificationMessage;
    private int delayInSeconds;

}
