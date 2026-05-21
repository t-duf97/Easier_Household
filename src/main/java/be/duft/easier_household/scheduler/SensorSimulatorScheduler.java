package be.duft.easier_household.scheduler;

import be.duft.easier_household.dal.repositories.DeviceRepository;
import be.duft.easier_household.dal.repositories.DeviceStateRepository;
import be.duft.easier_household.dl.entities.Device;
import be.duft.easier_household.dl.entities.DeviceState;
import be.duft.easier_household.dl.enums.ChangeSource;
import be.duft.easier_household.dl.enums.DeviceStatus;
import be.duft.easier_household.dl.enums.DeviceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
@Slf4j
@RequiredArgsConstructor
@Profile("dev")
public class SensorSimulatorScheduler {
    private final DeviceRepository deviceRepository;
    private final DeviceStateRepository deviceStateRepository;
    private final Random rand =  new Random();

    @Scheduled(fixedRate = 60000)
    public void simulateTemperatureSensors(){
        List<Device> sensors = deviceRepository.findByDeviceType(DeviceType.SENSOR_TEMP);

        for (Device sensor : sensors) {
            boolean motionDetected = rand.nextDouble() < 0.3;
            DeviceStatus newStatus = motionDetected ? DeviceStatus.ON : DeviceStatus.OFF;

            if (sensor.getStatus() != newStatus) {
                sensor.setStatus(newStatus);
                sensor.setUpdatedAt(LocalDateTime.now());
                deviceRepository.save(sensor);

                DeviceState state = new DeviceState();
                state.setDevice(sensor);
                state.setStatus(newStatus);
                state.setChangeSource(ChangeSource.SCHEDULED);
                state.setChangedAt(LocalDateTime.now());
                deviceStateRepository.save(state);

                log.debug("Motion sensor '{}' : {}", sensor.getName(), motionDetected ? "DETECTED" : "CLEAR");
            }


        }
    }



}
