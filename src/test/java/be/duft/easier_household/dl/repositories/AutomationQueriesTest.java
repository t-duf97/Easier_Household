package be.duft.easier_household.dl.repositories;

import be.duft.easier_household.TestcontainersConfiguration;
import be.duft.easier_household.dl.entities.*;
import be.duft.easier_household.dl.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Requêtes JPQL custom d'AutomationRepository et AutomationTriggerRepository, sur PostgreSQL.
 * Chaque test est annulé en fin d'exécution (@DataJpaTest est transactionnel).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class AutomationQueriesTest {

    @Autowired private AutomationRepository automationRepository;
    @Autowired private AutomationTriggerRepository automationTriggerRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private HouseholdRepository householdRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private DeviceRepository deviceRepository;

    private User user;
    private Household household;
    private Device lamp;
    private Device sensor;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setFirstName("Test");
        user.setLastName("Requêtes");
        user.setEmail("queries-" + UUID.randomUUID() + "@test.local");
        user.setPasswordHash("not-a-real-hash");
        user = userRepository.save(user);

        household = new Household();
        household.setName("Maison");
        household.setTimeZone(ZoneId.of("Europe/Brussels"));
        household.setCreatedBy(user);
        household = householdRepository.save(household);

        Room room = new Room();
        room.setName("Pièce-" + UUID.randomUUID());
        room.setFloor(0);
        room.setRoomType(RoomType.OTHER);
        room.setHousehold(household);
        room = roomRepository.save(room);

        lamp = device(room, DeviceType.LIGHT);
        sensor = device(room, DeviceType.SENSOR_TEMP);
    }

    @Test
    void timeTrigger_matchesTheWholeMinuteAndTheDayOfWeek() {
        Automation weekdays = automation("07:15 en semaine", true, t -> {
            t.setType(TriggerType.TIME);
            t.setTimeValue(LocalTime.of(7, 15, 30));
            t.setDaysOfWeek("MON,TUE,WED,THU,FRI");
        });
        Automation everyDay = automation("07:15 tous les jours", true, t -> {
            t.setType(TriggerType.TIME);
            t.setTimeValue(LocalTime.of(7, 15));
        });
        automation("07:15 désactivée", false, t -> {
            t.setType(TriggerType.TIME);
            t.setTimeValue(LocalTime.of(7, 15));
        });

        assertThat(ids(automationRepository.findEnabledAutomationsWithTimeTrigger(LocalTime.of(7, 15), "WED")))
                .containsExactlyInAnyOrder(weekdays.getId(), everyDay.getId());
        assertThat(ids(automationRepository.findEnabledAutomationsWithTimeTrigger(LocalTime.of(7, 15), "SAT")))
                .containsExactly(everyDay.getId());
        assertThat(automationRepository.findEnabledAutomationsWithTimeTrigger(LocalTime.of(7, 16), "WED")).isEmpty();
    }

    @Test
    void deviceTrigger_matchesDeviceAndStatusOfEnabledAutomations() {
        Automation onLampOn = automation("lampe ON", true, t -> {
            t.setType(TriggerType.DEVICE_STATE_CHANGE);
            t.setDevice(lamp);
            t.setStatus(DeviceStatus.ON);
        });
        automation("lampe OFF", true, t -> {
            t.setType(TriggerType.DEVICE_STATE_CHANGE);
            t.setDevice(lamp);
            t.setStatus(DeviceStatus.OFF);
        });
        automation("lampe ON désactivée", false, t -> {
            t.setType(TriggerType.DEVICE_STATE_CHANGE);
            t.setDevice(lamp);
            t.setStatus(DeviceStatus.ON);
        });

        assertThat(ids(automationRepository.findEnabledAutomationsByDeviceTrigger(lamp.getId(), DeviceStatus.ON)))
                .containsExactly(onLampOn.getId());
        assertThat(automationRepository.findEnabledAutomationsByDeviceTrigger(sensor.getId(), DeviceStatus.ON)).isEmpty();
    }

    @Test
    void referencingDevices_findsTriggersConditionsAndActionsOnce() {
        Automation viaTrigger = automation("trigger lampe", true, t -> {
            t.setType(TriggerType.DEVICE_STATE_CHANGE);
            t.setDevice(lamp);
            t.setStatus(DeviceStatus.ON);
        });
        Automation viaConditionAndAction = automation("condition + action capteur", true, t -> {
            t.setType(TriggerType.TIME);
            t.setTimeValue(LocalTime.NOON);
        });
        AutomationCondition condition = new AutomationCondition();
        condition.setAutomation(viaConditionAndAction);
        condition.setType(ConditionType.SENSOR_VALUE);
        condition.setDevice(sensor);
        condition.setThresholdValue(20.0);
        condition.setThresholdOperator(ThresholdOperator.GT);
        viaConditionAndAction.getConditions().add(condition);
        viaConditionAndAction.getActions().getFirst().setDevice(sensor);
        automationRepository.save(viaConditionAndAction);

        assertThat(ids(automationRepository.findReferencingDevices(List.of(sensor.getId()))))
                .containsExactly(viaConditionAndAction.getId());
        assertThat(ids(automationRepository.findReferencingDevices(List.of(lamp.getId(), sensor.getId()))))
                .containsExactlyInAnyOrder(viaTrigger.getId(), viaConditionAndAction.getId());
    }

    @Test
    void triggerQueries_returnOnlyEnabledTriggersOfTheirType() {
        automation("seuil capteur", true, t -> {
            t.setType(TriggerType.SENSOR_THRESHOLD);
            t.setDevice(sensor);
            t.setThresholdValue(25.0);
            t.setThresholdOperator(ThresholdOperator.GT);
        });
        automation("cron", true, t -> {
            t.setType(TriggerType.TIME);
            t.setCronExpression("*/15 * * * *");
        });
        automation("cron désactivé", false, t -> {
            t.setType(TriggerType.TIME);
            t.setCronExpression("0 8 * * *");
        });
        automation("lever", true, t -> t.setType(TriggerType.SUNRISE));
        automation("coucher", true, t -> t.setType(TriggerType.SUNSET));

        assertThat(automationTriggerRepository.findEnabledSensorThresholdTriggers(sensor.getId())).hasSize(1);
        assertThat(automationTriggerRepository.findEnabledSensorThresholdTriggers(lamp.getId())).isEmpty();
        assertThat(automationTriggerRepository.findEnabledCronTriggers())
                .extracting(AutomationTrigger::getCronExpression).containsExactly("*/15 * * * *");
        assertThat(automationTriggerRepository.findEnabledSunTriggers())
                .extracting(AutomationTrigger::getType).containsExactlyInAnyOrder(TriggerType.SUNRISE, TriggerType.SUNSET);
    }

    private Automation automation(String name, boolean enabled, Consumer<AutomationTrigger> trigger) {
        Automation automation = new Automation();
        automation.setName(name);
        automation.setLogic(ConditionLogic.ALL);
        automation.setEnabled(enabled);
        automation.setCreatedBy(user);
        automation.setHousehold(household);

        AutomationTrigger t = new AutomationTrigger();
        t.setAutomation(automation);
        trigger.accept(t);
        automation.getTriggers().add(t);

        AutomationAction action = new AutomationAction();
        action.setAutomation(automation);
        action.setType(ActionType.SET_DEVICE_STATUS);
        action.setDevice(lamp);
        action.setStatus(DeviceStatus.ON);
        action.setExecutionOrder(1);
        automation.getActions().add(action);
        return automationRepository.save(automation);
    }

    private Device device(Room room, DeviceType type) {
        Device device = new Device();
        device.setName(type.name());
        device.setType(type);
        device.setStatus(DeviceStatus.OFF);
        device.setOnline(true);
        device.setAssignedRoom(room);
        return deviceRepository.save(device);
    }

    private static List<Long> ids(List<Automation> automations) {
        return automations.stream().map(Automation::getId).toList();
    }
}
