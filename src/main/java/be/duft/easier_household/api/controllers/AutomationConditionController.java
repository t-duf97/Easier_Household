package be.duft.easier_household.api.controllers;

import be.duft.easier_household.bll.services.AutomationConditionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class AutomationConditionController {
    private final AutomationConditionService automationConditionService;
}
