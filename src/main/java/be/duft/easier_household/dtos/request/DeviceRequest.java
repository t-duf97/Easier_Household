package be.duft.easier_household.dtos.request;

import be.duft.easier_household.dl.enums.ChangeSource;
import be.duft.easier_household.dl.enums.DeviceStatus;
import be.duft.easier_household.dl.enums.DeviceType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DeviceRequest {

    @NotBlank
    private String name;

    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    private DeviceStatus status;

    private String value;

    @Enumerated(EnumType.STRING)
    private ChangeSource source;


}
