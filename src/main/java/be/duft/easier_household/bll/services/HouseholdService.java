package be.duft.easier_household.bll.services;

import be.duft.easier_household.dal.repositories.DeviceRepository;
import be.duft.easier_household.dal.repositories.HouseholdMemberRepository;
import be.duft.easier_household.dal.repositories.HouseholdRepository;
import be.duft.easier_household.dl.entities.Household;
import be.duft.easier_household.dl.entities.HouseholdMember;
import be.duft.easier_household.dtos.responseDTO.HouseholdResponse;
import be.duft.easier_household.exceptions.ResourceNotFoundException;
import be.duft.easier_household.mappers.HouseholdMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class HouseholdService {

    private final HouseholdRepository householdRepository;
    private final HouseholdMemberRepository householdMemberRepository;
    private final HouseholdMapper householdMapper;

    public HouseholdResponse findById(Long id) {
        Household household = householdRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Household not found: " + id));

        Set<HouseholdMember> members = householdMemberRepository.findByHousehold(household);

        return householdMapper.fromEntityToResponse(household, members);
    }
}
