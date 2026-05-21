package be.duft.easier_household.dtos.response;

import be.duft.easier_household.dl.enums.ChangeSource;
import be.duft.easier_household.dl.enums.DeviceStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class DeviceStateResponse {

    private Long id;
    private Long deviceId;
    private DeviceStatus status;
    private String value;
    private LocalDateTime changedAt;
    private Long changedBy;
    private ChangeSource changeSource;

}
