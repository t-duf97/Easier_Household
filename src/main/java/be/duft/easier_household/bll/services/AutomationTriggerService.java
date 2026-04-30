package be.duft.easier_household.bll.services;

import be.duft.easier_household.dal.repositories.AutomationTriggerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AutomationTriggerService {

    private final AutomationTriggerRepository automationTriggerRepository;
}
