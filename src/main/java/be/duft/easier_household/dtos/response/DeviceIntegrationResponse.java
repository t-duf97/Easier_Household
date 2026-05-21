package be.duft.easier_household.dtos.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class DeviceIntegrationResponse {

    // Tjrs flou aujourd'hui comment va se comporter cette entité.
    private Long id;
    private String provider;
    private Long deviceId;
    private String externalDeviceId;
    private String extraConfig;
    private LocalDateTime createdAt;
}
