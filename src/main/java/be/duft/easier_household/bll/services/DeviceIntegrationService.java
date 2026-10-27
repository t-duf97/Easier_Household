package be.duft.easier_household.bll.services;


import be.duft.easier_household.dl.repositories.DeviceIntegrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeviceIntegrationService {

    private final DeviceIntegrationRepository deviceIntegrationRepository;
}
