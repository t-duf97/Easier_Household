package be.duft.easier_household.dl.entities;

import be.duft.easier_household.dl.enums.ConditionType;
import be.duft.easier_household.dl.enums.DeviceStatus;
import be.duft.easier_household.dl.enums.ThresholdOperator;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"automation", "device"}, callSuper = false)
@ToString(exclude = {"automation", "device"})
public class AutomationCondition extends BaseEntity {

    @Getter
    @Column(name = "automation_id", updatable = false, insertable = false)
    private Long automationId;

    @Enumerated(EnumType.STRING)
    @Getter @Setter
    @Column(nullable = false)
    private ConditionType conditionType;

    @Enumerated(EnumType.STRING)
    @Getter @Setter
    @Column(nullable = false)
    private DeviceStatus expectedDeviceStatus;

    @Getter @Setter
    @Column(nullable = true)
    private Double thresholdValue;

    @Enumerated(EnumType.STRING)
    @Getter @Setter
    @Column(nullable = true)
    private ThresholdOperator thresholdOperator;

    @Getter @Setter
    @Column(nullable = true)
    private LocalTime timeFrom;

    @Getter @Setter
    @Column(nullable = true)
    private LocalTime timeTo;

    @Getter @Setter
    @Column(nullable = true)
    private String daysOfWeek;
    // A parser dans le service.

    @Getter @Setter
    @Column(nullable = true)
    private int durationInMinutes;
    // Pour les conditions type : "Si inactif depuis x minutes"

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="automation_id")
    private Automation automation;

    @Getter @Setter
    @Column(name = "device_id", insertable = false, updatable = false)
    private Long deviceId;

    @Getter @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private Device device;

}
