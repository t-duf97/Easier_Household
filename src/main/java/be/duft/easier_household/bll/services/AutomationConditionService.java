package be.duft.easier_household.bll.services;

import be.duft.easier_household.dal.repositories.AutomationConditionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AutomationConditionService {

    private final AutomationConditionRepository automationConditionRepository;
}
