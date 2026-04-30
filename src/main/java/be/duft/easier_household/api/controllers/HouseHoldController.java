package be.duft.easier_household.api.controllers;

import be.duft.easier_household.bll.services.HouseholdService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class HouseHoldController {
    private final HouseholdService householdService;
}
