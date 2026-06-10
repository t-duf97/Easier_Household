package be.duft.easier_household.dtos.response;

import be.duft.easier_household.dl.entities.HouseholdMember;
import be.duft.easier_household.dl.entities.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter @Setter
public class HouseholdResponse {

//    @Convert(converter = ZoneIdConverter.class)
    private String timeZone;
    private Long id;
    private String name;
    private String createdByName;
    private LocalDateTime createdAt;
    private int memberCount;;
    private String address;
    private int roomCount;
}
