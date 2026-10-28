package be.duft.easier_household.dl.entities;


import be.duft.easier_household.dl.enums.RoomType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"household"}, callSuper = false)
@ToString(exclude = {"household"})
@Table(name="room", uniqueConstraints = @UniqueConstraint(
        name = "uk_room_household_name", columnNames = {"household_id", "name"}))
public class Room extends BaseEntity {

    @Getter @Setter
    @Column(nullable = false)
    private String name;

    @Getter @Setter
    @Column(nullable = false)
    private int floor;

    @Getter @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RoomType roomType;

    @Getter @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id")
    private Household household;



}
