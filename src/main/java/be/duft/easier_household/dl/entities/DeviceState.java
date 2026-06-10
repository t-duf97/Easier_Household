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

    @Enumerated(EnumType.STRING)
    @Getter @Setter
    @Column(nullable = false)
    private DeviceStatus status;

    @Getter @Setter
    @Column(nullable = true)
    private String value;

    @Getter @Setter
    @Column
    private ChangeSource changeSource;

    @Getter @Setter
    @Column
    private LocalDateTime changedAt;


    @Getter
    @Column(name="user_id", insertable = false, updatable = false)
    private Long changedBy;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    @Getter @Setter
    private User user;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name="device_id")
    @Getter @Setter
    private Device device;

}
