package be.duft.easier_household.dtos.requestDTO;

import be.duft.easier_household.dl.enums.DeviceStatus;
import be.duft.easier_household.dl.enums.ThresholdOperator;
import be.duft.easier_household.dl.enums.TriggerType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter @Setter
public class AutomationTriggerRequest {

    @NotNull
    private TriggerType triggerType;

    private LocalTime timeValue;

    private String daysOfWeek;

    @NotNull
    private DeviceStatus expectedStatus;

    private double thresholdValue;

    private ThresholdOperator thresholdOperator;

    private String cronExpression;

    private Long deviceId;




}
