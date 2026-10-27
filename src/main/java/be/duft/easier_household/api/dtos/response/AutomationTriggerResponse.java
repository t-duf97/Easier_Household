package be.duft.easier_household.api.dtos.response;

import be.duft.easier_household.dl.enums.DeviceStatus;
import be.duft.easier_household.dl.enums.ThresholdOperator;
import be.duft.easier_household.dl.enums.TriggerType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class AutomationTriggerResponse {
    private Long id;
    private Long deviceId;
    private Long automationId;
    private TriggerType type;
    private LocalTime timeValue;
    private String daysOfWeek;
    private DeviceStatus status;
    private Double thresholdValue;
    private ThresholdOperator thresholdOperator;
    private String cronExpression;
    private String deviceName;
}
