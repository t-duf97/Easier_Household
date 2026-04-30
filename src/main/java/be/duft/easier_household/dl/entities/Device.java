package be.duft.easier_household.dl.entities;

import be.duft.easier_household.dl.enums.DeviceType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@EqualsAndHashCode @ToString
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Getter @Setter
    @Column(nullable = false)
    private String name;

    @Getter @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    @Getter @Setter
    @Column(name="room_id", insertable = false, updatable = false)
    private Long room_id;

    @ManyToOne
    @JoinColumn(name = "assigned_room_id")
    @Getter @Setter
    private Room assignedRoom;


    @Getter @Setter
    @Column(nullable = true)
    private String currentValue;

    @Getter @Setter
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private boolean isOnline;

    @Getter @Setter
    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Getter @Setter
    @Column
    private LocalDateTime updatedAt;

}
