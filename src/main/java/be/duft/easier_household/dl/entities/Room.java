package be.duft.easier_household.dl.entities;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@EqualsAndHashCode @ToString
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Getter @Setter
    @Column(unique = true, nullable = false)
    private String name;

    @Getter @Setter
    @Column(nullable = false)
    private int floor;

    @Getter @Setter
    @Column(nullable = false)
    private LocalDateTime created_at;

    @Column(name = "household_id", insertable = false, updatable = false)
    @Getter
    private Long householdId;


    @Getter @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id")
    private Household household;



}
