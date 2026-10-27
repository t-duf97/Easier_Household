package be.duft.easier_household.dl.condition;

import be.duft.easier_household.dl.repositories.DeviceRepository;
import be.duft.easier_household.domain.condition.SensorState;
import be.duft.easier_household.domain.condition.SensorStatePort;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptateur : traduit le port SensorStatePort en une lecture JPA/PostgreSQL.
 * Toute la technique (DeviceRepository, entité Device) reste ici.
 */
@Component
public class JpaSensorStateAdapter implements SensorStatePort {

    private final DeviceRepository deviceRepository;

    public JpaSensorStateAdapter(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Override
    public Optional<SensorState> stateOf(Long deviceId) {
        return deviceRepository.findById(deviceId)
                .map(device -> new SensorState(device.getId(), device.getCurrentValue()));
    }
}
