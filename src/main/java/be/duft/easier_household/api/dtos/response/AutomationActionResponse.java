package be.duft.easier_household.api.dtos.response;

import be.duft.easier_household.dl.enums.ActionType;
import be.duft.easier_household.dl.enums.DeviceStatus;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AutomationActionResponse {

    private Long id;
    private Long automationId;
    private Long deviceId;
    private int executionOrder;
    private ActionType type;
    private DeviceStatus status;
    private String targetValue;
    private String notificationMsg;
    private int delayInSeconds;
    private String deviceName;

}
