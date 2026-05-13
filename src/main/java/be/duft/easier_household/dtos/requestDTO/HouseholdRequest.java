package be.duft.easier_household.dtos.requestDTO;


import be.duft.easier_household.converter.ZoneIdConverter;
import be.duft.easier_household.dl.entities.HouseholdMember;
import jakarta.persistence.Convert;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.ZoneId;
import java.util.Set;

@Getter @Setter
public class HouseholdRequest {

    @NotBlank
    private String name;

    @NotNull
//    @Convert(converter = ZoneIdConverter.class)
    private String timeZone;

    // Je dois implémenter une liste de prénoms, qui seront assignés lorsque des
    // utilisateurs rejoindront le household.
    private Set<HouseholdMember> members;


    private String address;

}
