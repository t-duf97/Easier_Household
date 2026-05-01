package be.duft.easier_household.dtos.responseDTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class DeviceIntegrationResponse {

    // Tjrs flou aujourd'hui comment va se comporter cette entité.
    private Long id;
    private String provider;
    private Long deviceId;
    private Long external_device_id;
    private String extra_config;
    private LocalDateTime created_at;
}
