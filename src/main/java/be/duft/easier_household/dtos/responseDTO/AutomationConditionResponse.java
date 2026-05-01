package be.duft.easier_household.dtos.responseDTO;

import be.duft.easier_household.dl.enums.ConditionType;
import be.duft.easier_household.dl.enums.DeviceStatus;
import be.duft.easier_household.dl.enums.ThresholdOperator;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;

@Getter @Setter
public class AutomationConditionResponse {

    private Long id;
    private Long automationId;
    private ConditionType conditionType;
    private DeviceStatus deviceStatus;
    private double thresholdValue;
    private ThresholdOperator thresholdOperator;
    private LocalTime timeFrom;
    private LocalTime timeTo;
    private List<String> daysOfWeek;
    private int durationInMinutes;

}
