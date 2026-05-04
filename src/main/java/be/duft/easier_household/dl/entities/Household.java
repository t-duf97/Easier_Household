package be.duft.easier_household.dl.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@EqualsAndHashCode@ToString
public class Household extends BaseEntity {

    @Getter @Setter
    @Column(nullable = false)
    private String name;

    @Getter @Setter
    @Column(nullable = false)
    private ZoneId timeZone;


    @ManyToOne
    @JoinColumn(name = "created_by_id")
    @Getter @Setter
    private User createdBy;

    @Column
    @Getter @Setter
    private Set<HouseholdMember> members = new HashSet<>();



}
