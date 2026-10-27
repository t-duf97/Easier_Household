package be.duft.easier_household.api.dtos.response;

import be.duft.easier_household.dl.enums.ConditionType;
import be.duft.easier_household.dl.enums.DeviceStatus;
import be.duft.easier_household.dl.enums.ThresholdOperator;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter @Setter
public class AutomationConditionResponse {

    private Long id;
    private Long automationId;
    private Long deviceId;
    private ConditionType type;
    private DeviceStatus status;
    private Double thresholdValue;
    private ThresholdOperator thresholdOperator;
    private LocalTime timeFrom;
    private LocalTime timeTo;
    private String daysOfWeek;
    private Integer durationInMinutes;
    private String deviceName;

}
