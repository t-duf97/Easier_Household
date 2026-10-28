package be.duft.easier_household.bll.mappers;

import be.duft.easier_household.dl.entities.Household;
import be.duft.easier_household.dl.entities.HouseholdMember;
import be.duft.easier_household.dl.entities.Room;
import be.duft.easier_household.api.dtos.request.HouseholdRequest;
import be.duft.easier_household.api.dtos.response.HouseholdResponse;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.List;
import java.util.Set;

@Component
public class HouseholdMapper {

    public HouseholdResponse fromEntityToResponse(Household household, Set<HouseholdMember> members, List<Room> rooms) {
        HouseholdResponse householdResponse = new HouseholdResponse();
        householdResponse.setId(household.getId());
        householdResponse.setName(household.getName());
        householdResponse.setCreatedAt(household.getCreatedAt());
        householdResponse.setCreatedByName(
                household.getCreatedBy().getFirstName() + " " + household.getCreatedBy().getLastName()
        );
        householdResponse.setTimeZone(household.getTimeZone().getId());
        householdResponse.setAddress(household.getAddress());
        householdResponse.setLatitude(household.getLatitude());
        householdResponse.setLongitude(household.getLongitude());
        householdResponse.setMemberCount(members.size());
        householdResponse.setRoomCount(rooms.size());
        return householdResponse;
    }

    public Household fromRequesttoEntity(HouseholdRequest householdRequest) {
        Household household = new Household();
        household.setName(householdRequest.getName());
        household.setTimeZone(ZoneId.of(householdRequest.getTimeZone()));
        household.setAddress(householdRequest.getAddress());
        household.setLatitude(householdRequest.getLatitude());
        household.setLongitude(householdRequest.getLongitude());
        return household;
    }

    public void updateEntityFromRequest(HouseholdRequest request, Household household) {
        if (request.getName() != null) {
            household.setName(request.getName());
        } else household.setName(household.getName());
        if (request.getTimeZone() != null) {
            household.setTimeZone(ZoneId.of(request.getTimeZone()));
        } else household.setTimeZone(household.getTimeZone());
        if (request.getAddress() != null) {
            household.setAddress(request.getAddress());
        } else household.setAddress(household.getAddress());
        if (request.getLatitude() != null || request.getLongitude() != null) {
            household.setLatitude(request.getLatitude());
            household.setLongitude(request.getLongitude());
        }

    }
}
