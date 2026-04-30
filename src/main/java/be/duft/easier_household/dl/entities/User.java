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
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Integer id;

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
    @Column(nullable = false)
    LocalDateTime created_at;

    @Getter @Setter
    @Column
    LocalDateTime updated_at;

    @Getter @Setter
    @Column()
    private Set<HouseHoldMember> memberships = new HashSet<>();

}
