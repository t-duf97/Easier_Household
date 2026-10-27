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

    @Enumerated(EnumType.STRING)
    @Getter @Setter
    @Column(nullable = false)
    private ConditionType type;

    @Enumerated(EnumType.STRING)
    @Getter @Setter
    @Column(nullable = false)
    private DeviceStatus status;

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
    private Integer durationInMinutes;
    // Pour les conditions type : "Si inactif depuis x minutes"

    @Getter @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="automation_id")
    private Automation automation;

    @Getter @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private Device device;

}
