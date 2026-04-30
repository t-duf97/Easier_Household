package be.duft.easier_household.bll.services;

import be.duft.easier_household.dal.repositories.DeviceRepository;
import be.duft.easier_household.dal.repositories.HouseholdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HouseholdService {

    private final HouseholdRepository householdRepository;
}
