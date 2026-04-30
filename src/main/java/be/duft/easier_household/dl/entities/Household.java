package be.duft.easier_household.dl.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@EqualsAndHashCode@ToString
public class Household {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Getter @Setter
    @Column(nullable = false)
    private String name;

    @Getter @Setter
    @Column(nullable = false)
    private ZoneId timezone;


    @ManyToOne
    @JoinColumn(name = "created_by_id")
    @Getter @Setter
    private User createdBy;

    @Getter @Setter
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    @Getter @Setter
    private Set<HouseHoldMember> members = new HashSet<>();



}
