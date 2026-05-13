package be.duft.easier_household.dl.entities;


import be.duft.easier_household.dl.enums.RoomType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"household"}, callSuper = false)
@ToString(exclude = {"household"})
@Table(name="room")
public class Room extends BaseEntity {

    @Getter @Setter
    @Column(unique = true, nullable = false)
    private String name;

    @Getter @Setter
    @Column(nullable = false)
    private int floor;

    @Column(name = "household_id", insertable = false, updatable = false)
    @Getter
    private Long householdId;

    @Getter @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RoomType roomType;

    @Getter @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id")
    private Household household;



}
