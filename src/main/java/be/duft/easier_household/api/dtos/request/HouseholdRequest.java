package be.duft.easier_household.api.dtos.request;


import be.duft.easier_household.dl.entities.HouseholdMember;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter @Setter
public class HouseholdRequest {

    private String name;

//    @Convert(converter = ZoneIdConverter.class)
    private String timeZone;

    private Set<HouseholdMember> members;


    private String address;

}
