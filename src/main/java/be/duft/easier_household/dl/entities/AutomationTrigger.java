package be.duft.easier_household.dl.entities;


import be.duft.easier_household.dl.enums.DeviceStatus;
import be.duft.easier_household.dl.enums.ThresholdOperator;
import be.duft.easier_household.dl.enums.TriggerType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"automation", "device"}, callSuper = false)
@ToString(exclude = {"automation", "device"})
public class AutomationTrigger extends BaseEntity {


    @Getter @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TriggerType triggerType;

    @Getter @Setter
    @Column(nullable = true) // Uniquement utilisé si le trigger_type est TIME
    private LocalTime timeValue;

    @Getter @Setter
    @Column(nullable = true)
    private String daysOfWeek;
    // Questionner le fonctionnement des jours de la semaine pour l'automatisation
    // Au moment de la mise en place des automations

    @Getter @Setter
    @Column
    private DeviceStatus expectedStatus;
    // Statut expected par le trigger (soit avant le déclenchement, soit après. A paramétrer selon le trigger)

    @Getter @Setter
    @Column(nullable = true)
    private Double thresholdValue;
    // Exemple 25.0 pour une limite de température dans un sensor/threshold

    @Enumerated(EnumType.STRING)
    @Getter @Setter
    @Column(nullable = true)
    private ThresholdOperator thresholdOperator;
    // Utile uniquement dans le cas d'une comparaison avec une valeur et d'une thresholdvalue

    @Getter @Setter
    @Column(nullable = true)
    private String cronExpression;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="automation_id")
    @Getter @Setter
    private Automation automation;


    @Getter @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private Device device;


}
