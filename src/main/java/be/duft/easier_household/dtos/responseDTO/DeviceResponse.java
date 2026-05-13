package be.duft.easier_household.dtos.responseDTO;

import be.duft.easier_household.dl.entities.Room;
import be.duft.easier_household.dl.enums.DeviceStatus;
import be.duft.easier_household.dl.enums.DeviceType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class DeviceResponse {

    private Long id;
    private String name;
    private DeviceType deviceType;
    private Long roomId;
    private String currentValue;
    private boolean isOnline;
    private DeviceStatus deviceStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
