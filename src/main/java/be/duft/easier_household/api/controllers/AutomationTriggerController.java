package be.duft.easier_household.api.controllers;


import be.duft.easier_household.bll.services.AutomationTriggerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AutomationTriggerController {
    private final AutomationTriggerService automationTriggerService;
}
