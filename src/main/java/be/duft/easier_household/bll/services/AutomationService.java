package be.duft.easier_household.bll.services;

import be.duft.easier_household.dal.repositories.AutomationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AutomationService {

    private final AutomationRepository automationRepository;

}
