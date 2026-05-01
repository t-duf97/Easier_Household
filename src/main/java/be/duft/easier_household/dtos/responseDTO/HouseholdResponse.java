package be.duft.easier_household.dtos.responseDTO;

import be.duft.easier_household.dl.entities.HouseholdMember;
import be.duft.easier_household.dl.entities.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

@Getter @Setter
public class HouseholdResponse {

    private Long id;
    private String name;
    private ZoneId timeZone;
    private User createdBy;
    private LocalDateTime createdAt;
    private Set<HouseholdMember> members = new HashSet<>();
}
