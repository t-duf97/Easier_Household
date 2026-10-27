package be.duft.easier_household.dl.entities;

import be.duft.easier_household.dl.converter.ZoneIdConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZoneId;

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

    @Column
    @Getter @Setter
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    @Getter @Setter
    private User createdBy;




}
