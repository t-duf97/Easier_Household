package be.duft.easier_household.dtos.requestDTO;

import be.duft.easier_household.dl.entities.Room;
import be.duft.easier_household.dl.enums.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DeviceRequest {

    @NotBlank
    private String name;

    @NotNull
    private DeviceType deviceType;

    @NotNull
    private Room assignedRoom;

}
