package be.duft.easier_household.dl.entities;


import be.duft.easier_household.dl.enums.ActionType;
import be.duft.easier_household.dl.enums.DeviceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@NoArgsConstructor
@EqualsAndHashCode @ToString
public class AutomationAction extends BaseEntity {

    @Getter
    @Column(nullable = false, name="automation_id")
    private Long automationId;

    @Getter @Setter
    @Column(nullable = false, columnDefinition = "DEFAULT VALUE 1")
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
    @Column(nullable = true, columnDefinition = "DEFAULT VALUE 0")
    private int delayInSeconds;


    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name="automation_id", nullable = false)
    private List<Automation> automations;
}
