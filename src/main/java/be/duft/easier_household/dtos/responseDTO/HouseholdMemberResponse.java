package be.duft.easier_household.dtos.responseDTO;

import be.duft.easier_household.dl.embeddable.HouseholdMemberId;
import be.duft.easier_household.dl.entities.Household;
import be.duft.easier_household.dl.entities.User;
import be.duft.easier_household.dl.enums.Role;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class HouseholdMemberResponse {

    private HouseholdMemberId id;
    private Role role;
    private User user;
    private Household household;
    private LocalDateTime joinedAt;
}
