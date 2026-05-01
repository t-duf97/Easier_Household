package be.duft.easier_household.dtos.responseDTO;

import be.duft.easier_household.dl.enums.DeviceStatus;
import be.duft.easier_household.dl.enums.ThresholdOperator;
import be.duft.easier_household.dl.enums.TriggerType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class AutomationTriggerResponse {
    private Long id;
    private TriggerType triggerType;
    private LocalDateTime time_value;
    private List<String> daysOfWeek;
    private DeviceStatus expectedStatus;
    private double thresholdValue;
    private ThresholdOperator thresholdOperator;
}
