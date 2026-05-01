package be.duft.easier_household.dtos.requestDTO;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.ZoneId;
import java.util.List;

@Getter @Setter
public class HouseholdRequest {

    @NotBlank
    private String name;

    @NotNull
    private ZoneId timeZone;

    // Je dois implémenter une liste de prénoms, qui seront assignés lorsque des
    // utilisateurs rejoindront le household.
    private List<String> members;

}
