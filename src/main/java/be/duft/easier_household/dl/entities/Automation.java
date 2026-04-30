package be.duft.easier_household.dl.entities;

import be.duft.easier_household.dl.enums.ConditionLogic;
import jakarta.persistence.*;
import jakarta.validation.groups.Default;
import lombok.*;
import org.springframework.aot.hint.annotation.RegisterReflection;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@EqualsAndHashCode @ToString
public class Automation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Getter
    @Column(name="household_id")
    private Long householdId;

    @Getter @Setter
    @Column(nullable = false)
    private String name;

    @Getter @Setter
    @Column(nullable = true)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR DEFAULT ALL")
    @Getter @Setter
    private ConditionLogic conditionLogic;

    // Pas sur pour le VARCHAR DEFAULT

    @Getter @Setter
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private boolean isEnabled;

    @Getter
    @Column(name="username_id") // M'a declenché une erreur au moment de faire la jointure?
    private Long createdBy;

    @Getter @Setter
    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Getter @Setter
    @Column()
    private LocalDateTime updatedAt;

    @Getter @Setter
    private LocalDateTime last_triggered_at;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    @Getter
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="household_id", updatable = false, insertable = false)
    @Getter
    private Household household;


}
