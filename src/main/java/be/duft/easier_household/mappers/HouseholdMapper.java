package be.duft.easier_household.mappers;

import be.duft.easier_household.dl.entities.Household;
import be.duft.easier_household.dl.entities.HouseholdMember;
import be.duft.easier_household.dtos.requestDTO.HouseholdRequest;
import be.duft.easier_household.dtos.responseDTO.HouseholdResponse;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.List;
import java.util.Set;

@Component
public class HouseholdMapper {

    public HouseholdResponse fromEntityToResponse(Household household) {
        HouseholdResponse householdResponse = new HouseholdResponse();
        householdResponse.setId(household.getId());
        householdResponse.setName(household.getName());
        householdResponse.setCreatedAt(household.getCreatedAt());
        householdResponse.setCreatedBy(household.getCreatedBy());
        householdResponse.setTimeZone(household.getTimeZone().getId());
        return householdResponse;
    }
    public HouseholdResponse fromEntityToResponse(Household household, Set<HouseholdMember> members) {
        HouseholdResponse householdResponse = new HouseholdResponse();
        householdResponse.setId(household.getId());
        householdResponse.setName(household.getName());
        householdResponse.setCreatedAt(household.getCreatedAt());
        householdResponse.setCreatedBy(household.getCreatedBy());
        householdResponse.setTimeZone(household.getTimeZone().getId());
        householdResponse.setMembers(members);
        return householdResponse;
    }

    public Household fromRequesttoEntity(HouseholdRequest householdRequest) {
        Household household = new Household();
        household.setName(householdRequest.getName());
        household.setTimeZone(ZoneId.of(householdRequest.getTimeZone()));
        return household;
    }

    public void updateEntityFromRequest(HouseholdRequest request, Household household) {
        if (request.getName() != null) {
            household.setName(request.getName());
        }
        if (request.getTimeZone() != null) {
            household.setTimeZone(ZoneId.of(request.getTimeZone()));
        }
        if (request.getAddress() != null) {
            household.setAddress(request.getAddress());
        }

    }
}
