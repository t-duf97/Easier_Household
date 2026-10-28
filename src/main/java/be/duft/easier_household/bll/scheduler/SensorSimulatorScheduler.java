package be.duft.easier_household.bll.scheduler;

import be.duft.easier_household.bll.services.SensorReadingService;
import be.duft.easier_household.dl.repositories.DeviceRepository;
import be.duft.easier_household.dl.entities.Device;
import be.duft.easier_household.dl.enums.ChangeSource;
import be.duft.easier_household.dl.enums.DeviceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.random.RandomGenerator;

/**
 * Simule les capteurs en dev : génère des relevés aléatoires et les transmet à {@link SensorReadingService},
 * comme le ferait une vraie source de capteurs.
 */
@Component
@Slf4j
@RequiredArgsConstructor
@Profile("dev")
public class SensorSimulatorScheduler {

    static final double MIN_CELSIUS = 18.0;
    static final double MAX_CELSIUS = 25.0;
    static final double DEFAULT_CELSIUS = 21.0;
    static final double MAX_STEP_CELSIUS = 0.5;
    static final double MOTION_PROBABILITY = 0.3;

    private final DeviceRepository deviceRepository;
    private final SensorReadingService sensorReadingService;
    private final RandomGenerator random = RandomGenerator.getDefault();

    @Scheduled(fixedRateString = "${simulator.temperature.rate:PT5M}")
    public void simulateTemperatureSensors() {
        for (Device sensor : deviceRepository.findByType(DeviceType.SENSOR_TEMP)) {
            double step = random.nextDouble(-MAX_STEP_CELSIUS, MAX_STEP_CELSIUS);
            double celsius = nextTemperature(sensor.getCurrentValue(), step);
            try {
                sensorReadingService.recordTemperature(sensor.getId(), celsius, ChangeSource.SCHEDULED);
                log.debug("Temperature sensor '{}' : {} °C", sensor.getName(), celsius);
            } catch (Exception e) {
                log.error("Temperature simulation failed for device id={} : {}", sensor.getId(), e.getMessage(), e);
            }
        }
    }

    @Scheduled(fixedRateString = "${simulator.motion.rate:PT1M}")
    public void simulateMotionSensors() {
        for (Device sensor : deviceRepository.findByType(DeviceType.SENSOR_MOTION)) {
            boolean detected = random.nextDouble() < MOTION_PROBABILITY;
            try {
                sensorReadingService.recordMotion(sensor.getId(), detected, ChangeSource.SCHEDULED);
                log.debug("Motion sensor '{}' : {}", sensor.getName(), detected ? "DETECTED" : "CLEAR");
            } catch (Exception e) {
                log.error("Motion simulation failed for device id={} : {}", sensor.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Marche aléatoire bornée : la valeur courante (ou {@value #DEFAULT_CELSIUS} si absente ou illisible)
     * plus {@code step}, ramenée dans [{@value #MIN_CELSIUS}, {@value #MAX_CELSIUS}] et arrondie au dixième.
     */
    static double nextTemperature(String currentValue, double step) {
        double current;
        try {
            current = currentValue == null ? DEFAULT_CELSIUS : Double.parseDouble(currentValue);
        } catch (NumberFormatException e) {
            current = DEFAULT_CELSIUS;
        }
        double next = Math.clamp(current + step, MIN_CELSIUS, MAX_CELSIUS);
        return Math.round(next * 10) / 10.0;
    }
}
