package be.duft.easier_household.dl.entities;

import be.duft.easier_household.dl.enums.ConditionLogic;
import jakarta.persistence.*;
import jakarta.validation.groups.Default;
import lombok.*;
import org.springframework.aot.hint.annotation.RegisterReflection;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"household","user"}, callSuper = false)
@ToString(exclude = {"household","user"})
public class Automation extends BaseEntity {


    @Getter @Setter
    @Column(nullable = false)
    private String name;

    @Getter @Setter
    @Column(nullable = true)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Getter @Setter
    private ConditionLogic conditionLogic;

    // Pas sur pour le VARCHAR DEFAULT

    @Getter @Setter
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isEnabled;

    @Getter @Setter
    private LocalDateTime lastTriggeredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    @Getter @Setter
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="household_id", updatable = false, insertable = false)
    @Getter @Setter
    private Household household;

    @OneToMany(mappedBy = "automation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter @Setter
    private List<AutomationTrigger> triggers = new ArrayList<>();

    @OneToMany(mappedBy = "automation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter @Setter
    private List<AutomationCondition> conditions = new ArrayList<>();

    @OneToMany(mappedBy = "automation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter @Setter
    private List<AutomationAction> actions = new ArrayList<>();


}
