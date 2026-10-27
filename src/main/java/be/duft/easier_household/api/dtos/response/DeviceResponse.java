package be.duft.easier_household.api.dtos.response;

import be.duft.easier_household.dl.enums.DeviceStatus;
import be.duft.easier_household.dl.enums.DeviceType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class DeviceResponse {

    private Long id;
    private String name;
    private DeviceType type;
    private Long roomId;
    private String currentValue;
    private boolean isOnline;
    private DeviceStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String roomName;
    private String householdName;
}
