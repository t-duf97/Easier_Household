package be.duft.easier_household.dl.entities;


import be.duft.easier_household.dl.enums.ActionType;
import be.duft.easier_household.dl.enums.DeviceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"automation","device"}, callSuper = false)
@ToString(exclude={"automation","device"})
public class AutomationAction extends BaseEntity {

    @Getter @Setter
    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 1")
    private int executionOrder;

    @Getter @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    @Getter @Setter
    @Enumerated(EnumType.STRING)
    private DeviceStatus targetStatus;

    @Getter @Setter
    @Column(nullable = true)
    private String targetValue;

    @Getter @Setter
    @Column(nullable = true)
    private String notificationMsg;

    @Getter @Setter
    @Column(nullable = true, columnDefinition = "INTEGER DEFAULT 0")
    private int delayInSeconds;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="automation_id", nullable = false)
    @Getter @Setter
    private Automation automation;


    @Getter @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private Device device;
}
