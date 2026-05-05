package be.duft.easier_household.dl.entities;

import be.duft.easier_household.dl.enums.ConditionLogic;
import jakarta.persistence.*;
import jakarta.validation.groups.Default;
import lombok.*;
import org.springframework.aot.hint.annotation.RegisterReflection;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"household","user"}, callSuper = false)
@ToString(exclude = {"household","user"})
public class Automation extends BaseEntity {

    @Getter
    @Column(name="household_id", updatable = false,  insertable = false)
    private Long householdId;

    @Getter @Setter
    @Column(nullable = false)
    private String name;

    @Getter @Setter
    @Column(nullable = true)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50) DEFAULT 'ALL'")
    @Getter @Setter
    private ConditionLogic conditionLogic;

    // Pas sur pour le VARCHAR DEFAULT

    @Getter @Setter
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isEnabled;

    @Getter
    @Column(name="user_id", updatable = false, insertable = false) // M'a declenché une erreur au moment de faire la jointure?
    private Long createdBy;

    @Getter @Setter
    private LocalDateTime lastTriggeredAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    @Getter
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="household_id", updatable = false, insertable = false)
    @Getter
    private Household household;


}
