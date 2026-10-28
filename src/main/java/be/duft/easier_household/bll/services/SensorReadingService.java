package be.duft.easier_household.bll.services;

import be.duft.easier_household.bll.events.DeviceStatusChangedEvent;
import be.duft.easier_household.bll.events.DeviceValueChangedEvent;
import be.duft.easier_household.bll.exceptions.ResourceNotFoundException;
import be.duft.easier_household.dl.entities.Device;
import be.duft.easier_household.dl.entities.DeviceState;
import be.duft.easier_household.dl.enums.ChangeSource;
import be.duft.easier_household.dl.enums.DeviceStatus;
import be.duft.easier_household.dl.enums.DeviceType;
import be.duft.easier_household.dl.repositories.DeviceRepository;
import be.duft.easier_household.dl.repositories.DeviceStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Point d'entrée unique des relevés de capteurs, quelle que soit leur source
 * (simulateur en dev, future API capteurs). Chaque relevé est historisé.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class SensorReadingService {

    private final DeviceRepository deviceRepository;
    private final DeviceStateRepository deviceStateRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Enregistre une température (°C) dans {@code currentValue}, au format numérique lu par les conditions SENSOR_VALUE.
     */
    public void recordTemperature(Long deviceId, double celsius, ChangeSource source) {
        Device sensor = sensorOfType(deviceId, DeviceType.SENSOR_TEMP);
        String previousValue = sensor.getCurrentValue();
        String newValue = String.format(Locale.ROOT, "%.1f", celsius);

        sensor.setCurrentValue(newValue);
        sensor.setUpdatedAt(LocalDateTime.now());
        deviceRepository.save(sensor);
        recordState(sensor, source);
        if (!newValue.equals(previousValue)) {
            eventPublisher.publishEvent(new DeviceValueChangedEvent(sensor.getId(), previousValue, newValue));
        }
    }

    /**
     * Mouvement détecté = ON, absence = OFF. Un changement de statut déclenche les automatisations DEVICE_STATE_CHANGE.
     */
    public void recordMotion(Long deviceId, boolean detected, ChangeSource source) {
        Device sensor = sensorOfType(deviceId, DeviceType.SENSOR_MOTION);
        DeviceStatus newStatus = detected ? DeviceStatus.ON : DeviceStatus.OFF;
        if (sensor.getStatus() == newStatus) {
            return;
        }

        sensor.setStatus(newStatus);
        sensor.setUpdatedAt(LocalDateTime.now());
        deviceRepository.save(sensor);
        recordState(sensor, source);
        eventPublisher.publishEvent(new DeviceStatusChangedEvent(sensor.getId(), newStatus));
    }

    private Device sensorOfType(Long deviceId, DeviceType expectedType) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found: " + deviceId));
        if (device.getType() != expectedType) {
            throw new IllegalArgumentException("Device " + deviceId + " is " + device.getType() + ", expected " + expectedType);
        }
        return device;
    }

    private void recordState(Device device, ChangeSource source) {
        DeviceState state = new DeviceState();
        state.setDevice(device);
        state.setStatus(device.getStatus());
        state.setValue(device.getCurrentValue());
        state.setChangeSource(source);
        state.setChangedAt(LocalDateTime.now());
        deviceStateRepository.save(state);
    }
}
