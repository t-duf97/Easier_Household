package be.duft.easier_household.dl.entities;

import be.duft.easier_household.dl.enums.DeviceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@EqualsAndHashCode @ToString
public class DeviceState extends BaseEntity {

    @Column(name="device_id", nullable = false,  unique = true)
    @Getter
    private Long deviceId;

    @Enumerated(EnumType.STRING)
    @Getter @Setter
    @Column(nullable = false)
    private DeviceStatus status;

    @Getter @Setter
    @Column(nullable = false)
    private String value;

    @Getter
    @Column(name="user_id") // M'a declenché une erreur au moment de faire la jointure?
    private Long changedBy;

    // A voir ce que ça donne. Je ne sais pas si ma jointure est bien faite sur la table USer
    // A voir aussi si à la suppression d'un user l'historique ne part pas en cacahuète
    // yeehaw


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    @Getter @Setter
    private User user;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name="device_id")
    @Getter @Setter
    private Device device;

}
