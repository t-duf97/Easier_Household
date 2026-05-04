package be.duft.easier_household.dl.entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@ToString @EqualsAndHashCode
public class User extends BaseEntity {


    @Getter @Setter
    @Column(nullable = false, name="username")
    private String username;

    @Getter @Setter
    @Column(nullable = false, unique = true)
    @Email
    private String email;

    @Getter @Setter
    @Column(nullable = false)
    private String passwordHash;

    @Getter @Setter
    @Column()
    private Set<HouseholdMember> memberships = new HashSet<>();

}
