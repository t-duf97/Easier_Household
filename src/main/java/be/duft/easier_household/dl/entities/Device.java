package be.duft.easier_household.dl.entities;

import be.duft.easier_household.dl.enums.DeviceStatus;
import be.duft.easier_household.dl.enums.DeviceType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"assignedRoom"}, callSuper = false)
@ToString(exclude = {"assignedRoom"})
@Table(name="device")
public class Device extends BaseEntity {

    @Getter @Setter
    @Column(nullable = false)
    private String name;

    @Getter @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    @Getter @Setter
    @Column(nullable = true)
    private String currentValue;

    @Getter @Setter
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private boolean isOnline;

    @Getter @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roomId")
    @Getter @Setter
    private Room assignedRoom;

}
