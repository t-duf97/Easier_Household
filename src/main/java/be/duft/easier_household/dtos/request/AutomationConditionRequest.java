package be.duft.easier_household.dtos.request;

import be.duft.easier_household.dl.enums.ConditionType;
import be.duft.easier_household.dl.enums.DeviceStatus;
import be.duft.easier_household.dl.enums.ThresholdOperator;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter @Setter
public class AutomationConditionRequest {

    @NotNull
    private ConditionType conditionType;

    @NotNull
    private DeviceStatus expectedDeviceStatus;


    private double thresholdValue;

    private ThresholdOperator thresholdOperator;

    private LocalTime timeFrom;

    private LocalTime timeTo;

    @NotNull
    private String daysOfWeek;

    private int durationInMinutes;

    private Long deviceId;

    private String deviceName;




}
