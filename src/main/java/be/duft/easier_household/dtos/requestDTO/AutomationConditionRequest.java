package be.duft.easier_household.dtos.requestDTO;

import be.duft.easier_household.dl.enums.ConditionType;
import be.duft.easier_household.dl.enums.DeviceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;

@Getter @Setter
public class AutomationConditionRequest {

    @NotNull
    private ConditionType conditionType;

    @NotNull
    private DeviceStatus expectedDeviceStatus;


    private double thresholdValue;

    private double threshldOperator;

    private LocalTime time_from;

    private LocalTime time_to;

    @NotNull
    private List<String> daysOfWeek;

    private int durationInMinutes;



}
