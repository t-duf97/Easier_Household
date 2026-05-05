package be.duft.easier_household.dl.entities;

import be.duft.easier_household.dl.enums.ChangeSource;
import be.duft.easier_household.dl.enums.DeviceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"device", "user"},callSuper = false)
@ToString(exclude = {"device", "user"})
public class DeviceState extends BaseEntity {

    @Column(name="device_id", insertable = false, updatable = false, nullable = false)
    @Getter
    private Long deviceId;

    @Enumerated(EnumType.STRING)
    @Getter @Setter
    @Column(nullable = false)
    private DeviceStatus status;

    @Getter @Setter
    @Column(nullable = true)
    private String value;

    @Getter @Setter
    private ChangeSource changeSource;

    @Getter
    @Column(name="user_id", insertable = false, updatable = false)
    private Long changedBy;

    // A voir ce que ça donne. Je ne sais pas si ma jointure est bien faite sur la table USer
    // A voir aussi si à la suppression d'un user l'historique ne part pas en cacahuète
    // yeehaw


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    @Getter @Setter
    private User user;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name="device_id")
    @Getter @Setter
    private Device device;

}
