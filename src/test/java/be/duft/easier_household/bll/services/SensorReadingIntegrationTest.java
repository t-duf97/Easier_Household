package be.duft.easier_household.bll.services;

import be.duft.easier_household.TestcontainersConfiguration;
import be.duft.easier_household.dl.entities.*;
import be.duft.easier_household.dl.enums.*;
import be.duft.easier_household.dl.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;

import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Chaîne capteurs -> moteur : un relevé de mouvement déclenche une automatisation
 * dont la condition SENSOR_VALUE lit la température relevée.
 */
@SpringBootTest(properties = "jwt.secret=dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLW9ubHktbm90LXJlYWwtMTIzNDU2")
@Import(TestcontainersConfiguration.class)
class SensorReadingIntegrationTest {

    @Autowired private SensorReadingService sensorReadingService;
    @Autowired private UserRepository userRepository;
    @Autowired private HouseholdRepository householdRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private DeviceRepository deviceRepository;
    @Autowired private DeviceStateRepository deviceStateRepository;
    @Autowired private AutomationRepository automationRepository;

    private Device thermometer;
    private Device motionSensor;
    private Device fan;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setFirstName("Test");
        user.setLastName("Capteurs");
        user.setEmail("sensors-" + UUID.randomUUID() + "@test.local");
        user.setPasswordHash("not-a-real-hash");
        user = userRepository.save(user);

        Household household = new Household();
        household.setName("Maison capteurs");
        household.setTimeZone(ZoneId.of("Europe/Brussels"));
        household.setCreatedBy(user);
        household = householdRepository.save(household);

        Room room = new Room();
        room.setName("Bureau-" + UUID.randomUUID());
        room.setFloor(1);
        room.setRoomType(RoomType.OTHER);
        room.setHousehold(household);
        room = roomRepository.save(room);

        thermometer = device(room, "Thermomètre", DeviceType.SENSOR_TEMP);
        motionSensor = device(room, "Détecteur", DeviceType.SENSOR_MOTION);
        fan = device(room, "Ventilateur", DeviceType.SWITCH);

        Automation automation = new Automation();
        automation.setName("Présence et > 23 °C -> ventilateur");
        automation.setLogic(ConditionLogic.ALL);
        automation.setEnabled(true);
        automation.setCreatedBy(user);
        automation.setHousehold(household);

        AutomationTrigger motion = new AutomationTrigger();
        motion.setAutomation(automation);
        motion.setType(TriggerType.DEVICE_STATE_CHANGE);
        motion.setDevice(motionSensor);
        motion.setStatus(DeviceStatus.ON);
        automation.getTriggers().add(motion);

        AutomationCondition hot = new AutomationCondition();
        hot.setAutomation(automation);
        hot.setType(ConditionType.SENSOR_VALUE);
        hot.setDevice(thermometer);
        hot.setThresholdValue(23.0);
        hot.setThresholdOperator(ThresholdOperator.GT);
        automation.getConditions().add(hot);

        AutomationAction fanOn = new AutomationAction();
        fanOn.setAutomation(automation);
        fanOn.setType(ActionType.SET_DEVICE_STATUS);
        fanOn.setDevice(fan);
        fanOn.setStatus(DeviceStatus.ON);
        fanOn.setExecutionOrder(1);
        automation.getActions().add(fanOn);
        automationRepository.save(automation);
    }

    @Test
    void motionWhileHot_triggersAutomation() {
        sensorReadingService.recordTemperature(thermometer.getId(), 24.2, ChangeSource.SCHEDULED);
        sensorReadingService.recordMotion(motionSensor.getId(), true, ChangeSource.SCHEDULED);

        assertThat(reload(thermometer).getCurrentValue()).isEqualTo("24.2");
        assertThat(reload(motionSensor).getStatus()).isEqualTo(DeviceStatus.ON);
        assertThat(reload(fan).getStatus()).isEqualTo(DeviceStatus.ON);
    }

    @Test
    void motionWhileCool_doesNotTriggerAutomation() {
        sensorReadingService.recordTemperature(thermometer.getId(), 21.0, ChangeSource.SCHEDULED);
        sensorReadingService.recordMotion(motionSensor.getId(), true, ChangeSource.SCHEDULED);

        assertThat(reload(fan).getStatus()).isEqualTo(DeviceStatus.OFF);
    }

    @Test
    void readings_areHistorisedWithTheirSource() {
        sensorReadingService.recordTemperature(thermometer.getId(), 19.5, ChangeSource.SCHEDULED);
        sensorReadingService.recordMotion(motionSensor.getId(), true, ChangeSource.EXTERNAL_API);
        sensorReadingService.recordMotion(motionSensor.getId(), true, ChangeSource.EXTERNAL_API);

        assertThat(deviceStateRepository.findByDeviceIdOrderByChangedAtDesc(thermometer.getId(), Pageable.unpaged()))
                .singleElement()
                .satisfies(s -> {
                    assertThat(s.getValue()).isEqualTo("19.5");
                    assertThat(s.getChangeSource()).isEqualTo(ChangeSource.SCHEDULED);
                });
        assertThat(deviceStateRepository.findByDeviceIdOrderByChangedAtDesc(motionSensor.getId(), Pageable.unpaged()))
                .as("un second ON identique n'est pas un changement")
                .singleElement()
                .satisfies(s -> assertThat(s.getChangeSource()).isEqualTo(ChangeSource.EXTERNAL_API));
    }

    @Test
    void reading_isRefusedForWrongDeviceType() {
        assertThatThrownBy(() -> sensorReadingService.recordTemperature(fan.getId(), 22.0, ChangeSource.SCHEDULED))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private Device device(Room room, String name, DeviceType type) {
        Device device = new Device();
        device.setName(name);
        device.setType(type);
        device.setStatus(DeviceStatus.OFF);
        device.setOnline(true);
        device.setAssignedRoom(room);
        return deviceRepository.save(device);
    }

    private Device reload(Device device) {
        return deviceRepository.findById(device.getId()).orElseThrow();
    }
}
