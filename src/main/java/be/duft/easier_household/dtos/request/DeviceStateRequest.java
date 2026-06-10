package be.duft.easier_household.dtos.request;

import be.duft.easier_household.dl.enums.DeviceStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DeviceStateRequest {


    @Enumerated(EnumType.STRING)
    private DeviceStatus status;

    private String value;

    private LocalDateTime changedAt;

}
