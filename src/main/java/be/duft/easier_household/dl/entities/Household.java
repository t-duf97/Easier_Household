package be.duft.easier_household.dl.entities;

import be.duft.easier_household.converter.ZoneIdConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class Household extends BaseEntity {

    @Getter @Setter
    @Column(nullable = false)
    private String name;

    @Getter @Setter
    @Column(nullable = false)
    @Convert(converter = ZoneIdConverter.class)
    private ZoneId timeZone;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @Getter @Setter
    private User createdBy;


}
