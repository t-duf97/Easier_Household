package be.duft.easier_household.api.controllers;

import be.duft.easier_household.bll.services.AutomationActionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AutomationActionController {

    private final AutomationActionService automationActionService;

}
