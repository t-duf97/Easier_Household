package be.duft.easier_household.dl.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@EqualsAndHashCode @ToString
@NoArgsConstructor
public class DeviceIntegration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Getter @Setter
    @Column(nullable = false)
    private String provider;

    @Getter @Setter
    @Column(name ="device_id", nullable = false)
    private Long deviceId;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="device_id")
    private Device device;

    @Getter @Setter
    @Column(nullable = false)
    private Long external_device_id;

    // Je ne sais pas encore comment le récupérer. ça risque de devenir un String/varchar

    @Getter @Setter
    @Column(nullable = false)
    private String credentials_ref;
    // Pareil. ici c'est plutot les login/token pour utiliser l'api mais aucune idée de comment procéder.

    @Getter @Setter
    @Column(nullable = true)
    private String extra_config;

    //Configuration nécessaire supplémentaire selon l'appareil. A voir selon l'intégration

    @Getter @Setter
    @Column(nullable = false)
    private LocalDateTime created_at;

}
