package be.duft.easier_household.dl.embeddable;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class HouseholdMemberId implements Serializable {
    private Long userId;
    private Long householdId;

}
