package be.duft.easier_household.mappers;

import be.duft.easier_household.dl.entities.Household;
import be.duft.easier_household.dl.entities.HouseholdMember;
import be.duft.easier_household.dtos.requestDTO.HouseholdRequest;
import be.duft.easier_household.dtos.responseDTO.HouseholdResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class HouseholdMapper {

    public HouseholdResponse fromEntityToResponse(Household household, Set<HouseholdMember> members) {
        HouseholdResponse householdResponse = new HouseholdResponse();
        householdResponse.setId(household.getId());
        householdResponse.setName(household.getName());
        householdResponse.setCreatedAt(household.getCreatedAt());
        householdResponse.setMembers(members);
        householdResponse.setCreatedBy(household.getCreatedBy());
        householdResponse.setTimeZone(household.getTimeZone());
        return householdResponse;
    }

    public Household fromRequesttoEntity(HouseholdRequest householdRequest, Set<HouseholdMember> members) {
        Household household = new Household();
        household.setName(householdRequest.getName());
        household.setTimeZone(householdRequest.getTimeZone());
        return household;
    }
}
