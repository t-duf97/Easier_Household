package be.duft.easier_household.dl.entities;

import be.duft.easier_household.dl.embeddable.HouseholdMemberId;
import be.duft.easier_household.dl.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
public class HouseHoldMember {

    @EmbeddedId
    @Getter
    private HouseholdMemberId householdMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @Getter
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("householdId")
    @Getter
    private Household household;

    @Getter @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Getter @Setter
    @Column(nullable = false)
    private LocalDateTime joinedAt;



}
