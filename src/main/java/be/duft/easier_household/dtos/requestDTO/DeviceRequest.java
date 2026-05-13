package be.duft.easier_household.dtos.requestDTO;

import be.duft.easier_household.dl.entities.DeviceState;
import be.duft.easier_household.dl.entities.Room;
import be.duft.easier_household.dl.enums.ChangeSource;
import be.duft.easier_household.dl.enums.DeviceStatus;
import be.duft.easier_household.dl.enums.DeviceType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jdk.jshell.Snippet;
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
