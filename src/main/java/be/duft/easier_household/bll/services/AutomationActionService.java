package be.duft.easier_household.bll.services;


import be.duft.easier_household.dal.repositories.AutomationActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AutomationActionService {

    private final AutomationActionRepository automationActionRepository;
}
