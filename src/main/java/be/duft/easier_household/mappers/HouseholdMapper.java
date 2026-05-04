package be.duft.easier_household.mappers;

import be.duft.easier_household.dl.entities.Household;
import be.duft.easier_household.dtos.requestDTO.HouseholdRequest;
import be.duft.easier_household.dtos.responseDTO.HouseholdResponse;
import org.springframework.stereotype.Component;

@Component
public class HouseholdMapper {

    public HouseholdResponse fromEntityToResponse(Household household) {
        HouseholdResponse householdResponse = new HouseholdResponse();
        householdResponse.setId(household.getId());
        householdResponse.setName(household.getName());
        householdResponse.setCreatedAt(household.getCreatedAt());
        householdResponse.setMembers(household.getMembers());
        householdResponse.setCreatedBy(household.getCreatedBy());
        householdResponse.setTimeZone(household.getTimeZone());
        return householdResponse;
    }

    public Household fromRequesttoEntity(HouseholdRequest householdRequest) {
        Household household = new Household();
        household.setName(householdRequest.getName());
        household.setTimeZone(householdRequest.getTimeZone());
        household.setMembers(householdRequest.getMembers());
        return household;
    }
}
