package be.duft.easier_household.dl.entities;


import be.duft.easier_household.dl.enums.DeviceStatus;
import be.duft.easier_household.dl.enums.ThresholdOperator;
import be.duft.easier_household.dl.enums.TriggerType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@EqualsAndHashCode @ToString
public class AutomationTrigger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Getter @Setter
    @Column(name="automation_id")
    private List<Long> automationIds;


    @Getter @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TriggerType triggerType;

    @Getter @Setter
    @Column(nullable = true) // Uniquement utilisé si le trigger_type est TIME
    private LocalDateTime timeValue;

    @Getter @Setter
    @Column(nullable = true)
    private List<String> daysOfWeek;

    // Questionner le fonctionnement des jours de la semaine pour l'automatisation
    // Au moment de la mise en place des automations

    @Getter @Setter
    @Column(nullable = false)
    private DeviceStatus expectedStatus;
    // Statut expected par le trigger (soit avant le déclenchement, soit après. A paramétrer selon le trigger)


    @Getter @Setter
    @Column(nullable = true)
    private double thresholdValue;
    // Exemple 25.0 pour une limite de température dans un sensor/threshold

    @Enumerated(EnumType.STRING)
    @Getter @Setter
    @Column(nullable = true)
    private ThresholdOperator thresholdOperator;
    // Utile uniquement dans le cas d'une comparaison avec une valeur et d'une thresholdvalue


    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name="automation_id")
    @Getter
    private List<Automation> automations = new ArrayList<>();




}
