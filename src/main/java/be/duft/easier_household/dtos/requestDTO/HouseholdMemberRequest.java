package be.duft.easier_household.dtos.requestDTO;

import be.duft.easier_household.dl.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HouseholdMemberRequest {

    @Getter @Setter
    private Role role;

    // Je ne sais pas encore à quoi ressemblera la page d'ajout de membre au foyer
    // A ce moment la j'agrémente cette page.
}
