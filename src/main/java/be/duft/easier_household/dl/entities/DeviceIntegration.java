package be.duft.easier_household.dl.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"device"}, callSuper = false)
@ToString(exclude = {"device"})
public class DeviceIntegration extends BaseEntity {

    @Getter @Setter
    @Column(nullable = false)
    private String provider;

    @Getter @Setter
    @Column(name ="device_id", insertable = false, updatable = false, nullable = false)
    private Long deviceId;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="device_id")
    @Getter @Setter
    private Device device;

    @Getter @Setter
    @Column(nullable = false)
    private String externalDeviceId;

    @Getter @Setter
    @Column(nullable = false)
    private String credentialsRef;
    // Pareil. ici c'est plutot les login/token pour utiliser l'api mais aucune idée de comment procéder.

    @Getter @Setter
    @Column(nullable = true)
    private String extraConfig;

    //Configuration nécessaire supplémentaire selon l'appareil. A voir selon l'intégration

}
