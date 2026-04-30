package be.duft.easier_household.api.controllers;

import be.duft.easier_household.bll.services.AutomationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AutomationController {

    private final AutomationService automationService;

}
