package be.duft.easier_household.api.dtos.request;

import be.duft.easier_household.dl.enums.ChangeSource;
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

    @NotNull
    @Enumerated(EnumType.STRING)
    private DeviceStatus status;

    private String value;

    @Enumerated(EnumType.STRING)
    private ChangeSource source = ChangeSource.MANUAL;


}
