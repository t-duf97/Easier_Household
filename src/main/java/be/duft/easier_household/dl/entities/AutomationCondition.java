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
@EqualsAndHashCode @ToString
public class AutomationCondition extends BaseEntity {

    @Getter
    @Column(name = "automation_id")
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
    private double thresholdValue;

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
    private List<String> daysOfWeek;
    // Je ne sais pas encore comment je vais procéder pour les jours.

    @Getter @Setter
    @Column(nullable = true)
    private int durationInMinutes;
    // Pour les conditions type : "Si inactif depuis x minutes"

    @Getter
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name="automation_id")
    private List<Automation> automations = new ArrayList<>();


}
