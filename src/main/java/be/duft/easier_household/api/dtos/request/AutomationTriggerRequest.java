package be.duft.easier_household.api.dtos.request;

import be.duft.easier_household.dl.enums.DeviceStatus;
import be.duft.easier_household.dl.enums.ThresholdOperator;
import be.duft.easier_household.dl.enums.TriggerType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter @Setter
public class AutomationTriggerRequest {

    @NotNull
    private TriggerType type;

    private LocalTime timeValue;

    private String daysOfWeek;

    @NotNull
    private DeviceStatus status;

    private Double thresholdValue;

    private ThresholdOperator thresholdOperator;

    private String cronExpression;

    private Long deviceId;




}
