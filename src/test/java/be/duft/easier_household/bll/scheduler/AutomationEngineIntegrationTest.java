package be.duft.easier_household.bll.scheduler;

import be.duft.easier_household.TestcontainersConfiguration;
import be.duft.easier_household.api.dtos.request.DeviceStateRequest;
import be.duft.easier_household.bll.services.DeviceService;
import be.duft.easier_household.dl.entities.*;
import be.duft.easier_household.dl.enums.*;
import be.duft.easier_household.dl.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Moteur d'automatisations de bout en bout, sur une vraie base PostgreSQL (Testcontainers).
 * Les méthodes de test ne sont volontairement pas @Transactional : les automatisations réactives
 * ne partent qu'après le commit du changement d'état, comme en production.
 */
@SpringBootTest(properties = "jwt.secret=dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLW9ubHktbm90LXJlYWwtMTIzNDU2")
@Import(TestcontainersConfiguration.class)
class AutomationEngineIntegrationTest {

    @Autowired private DeviceService deviceService;
    @Autowired private AutomationScheduler automationScheduler;
    @Autowired private UserRepository userRepository;
    @Autowired private HouseholdRepository householdRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private DeviceRepository deviceRepository;
    @Autowired private DeviceStateRepository deviceStateRepository;
    @Autowired private AutomationRepository automationRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    private static final String BROKEN_VALUE = "BROKEN";

    private User user;
    private Household household;
    private Room room;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString();

        user = new User();
        user.setFirstName("Test");
        user.setLastName("Engine");
        user.setEmail("engine-" + suffix + "@test.local");
        user.setPasswordHash("not-a-real-hash");
        user = userRepository.save(user);

        household = new Household();
        household.setName("Maison test");
        household.setTimeZone(ZoneId.of("Europe/Brussels"));
        household.setCreatedBy(user);
        household = householdRepository.save(household);

        room = new Room();
        room.setName("Salon-" + suffix);
        room.setFloor(0);
        room.setRoomType(RoomType.LIVING_ROOM);
        room.setHousehold(household);
        room = roomRepository.save(room);
    }

    @Test
    void deviceStateChange_triggersActionOnAnotherDevice() {
        Device switchA = device("Interrupteur");
        Device lampB = device("Lampe");
        Automation automation = automation("A ON -> B ON");
        automation.getTriggers().add(stateTrigger(automation, switchA, DeviceStatus.ON));
        automation.getActions().add(setStatusAction(automation, lampB, DeviceStatus.ON, 1));
        automationRepository.save(automation);

        deviceService.changeState(switchA.getId(), stateRequest(DeviceStatus.ON));

        assertThat(statusOf(lampB)).isEqualTo(DeviceStatus.ON);
        assertThat(deviceStateRepository.findByDeviceIdOrderByChangedAtDesc(lampB.getId(), Pageable.ofSize(1)))
                .singleElement()
                .satisfies(state -> {
                    assertThat(state.getStatus()).isEqualTo(DeviceStatus.ON);
                    assertThat(state.getChangeSource()).isEqualTo(ChangeSource.AUTOMATION);
                });
    }

    @Test
    void failingAutomation_doesNotRollBackManualChangeNorOtherAutomations() {
        Device switchA = device("Interrupteur");
        Device lampB = device("Lampe");
        Device thermostat = device("Thermostat");

        rejectBrokenValuesInDatabase();

        Automation failing = automation("écriture refusée par la base");
        failing.getTriggers().add(stateTrigger(failing, switchA, DeviceStatus.ON));
        AutomationAction brokenWrite = new AutomationAction();
        brokenWrite.setAutomation(failing);
        brokenWrite.setType(ActionType.SET_DEVICE_VALUE);
        brokenWrite.setDevice(thermostat);
        brokenWrite.setTargetValue(BROKEN_VALUE);
        brokenWrite.setExecutionOrder(1);
        failing.getActions().add(brokenWrite);
        automationRepository.save(failing);

        Automation working = automation("A ON -> B ON");
        working.getTriggers().add(stateTrigger(working, switchA, DeviceStatus.ON));
        working.getActions().add(setStatusAction(working, lampB, DeviceStatus.ON, 1));
        automationRepository.save(working);

        deviceService.changeState(switchA.getId(), stateRequest(DeviceStatus.ON));

        assertThat(statusOf(switchA)).isEqualTo(DeviceStatus.ON);
        assertThat(statusOf(lampB)).isEqualTo(DeviceStatus.ON);
        assertThat(deviceRepository.findById(thermostat.getId()).orElseThrow().getCurrentValue()).isNull();
    }

    /**
     * Contrainte propre au test : fait échouer en base (et non en Java) l'écriture d'une valeur précise,
     * comme une vraie erreur SQL qui rend la transaction courante inutilisable.
     */
    private void rejectBrokenValuesInDatabase() {
        jdbcTemplate.execute("ALTER TABLE device DROP CONSTRAINT IF EXISTS test_reject_broken_value");
        jdbcTemplate.execute("ALTER TABLE device ADD CONSTRAINT test_reject_broken_value "
                + "CHECK (current_value IS DISTINCT FROM '" + BROKEN_VALUE + "')");
    }

    @Test
    void unchangedStatus_doesNotTriggerAutomations() {
        Device switchA = device("Interrupteur");
        Device lampB = device("Lampe");
        Automation automation = automation("A OFF -> B ON");
        automation.getTriggers().add(stateTrigger(automation, switchA, DeviceStatus.OFF));
        automation.getActions().add(setStatusAction(automation, lampB, DeviceStatus.ON, 1));
        automationRepository.save(automation);

        deviceService.changeState(switchA.getId(), stateRequest(DeviceStatus.OFF));

        assertThat(statusOf(lampB)).isEqualTo(DeviceStatus.OFF);
    }

    @Test
    void conditionWithoutDevice_isSavedAndEvaluated() {
        Device switchA = device("Interrupteur");
        Device lampB = device("Lampe");
        Automation automation = automation("A ON -> B ON, tous les jours");
        automation.getTriggers().add(stateTrigger(automation, switchA, DeviceStatus.ON));
        AutomationCondition everyDay = new AutomationCondition();
        everyDay.setAutomation(automation);
        everyDay.setType(ConditionType.DAY_OF_WEEK);
        everyDay.setDaysOfWeek("MON,TUE,WED,THU,FRI,SAT,SUN");
        automation.getConditions().add(everyDay);
        automation.getActions().add(setStatusAction(automation, lampB, DeviceStatus.ON, 1));
        automationRepository.save(automation);

        deviceService.changeState(switchA.getId(), stateRequest(DeviceStatus.ON));

        assertThat(statusOf(lampB)).isEqualTo(DeviceStatus.ON);
    }

    @Test
    void timeTrigger_runsDuringItsMinuteOnly() {
        Device lamp = device("Lampe");
        Automation automation = automation("Réveil 07:15");
        AutomationTrigger trigger = new AutomationTrigger();
        trigger.setAutomation(automation);
        trigger.setType(TriggerType.TIME);
        trigger.setTimeValue(LocalTime.of(7, 15));
        trigger.setDaysOfWeek("MON,TUE,WED,THU,FRI");
        automation.getTriggers().add(trigger);
        automation.getActions().add(setStatusAction(automation, lamp, DeviceStatus.ON, 1));
        automationRepository.save(automation);

        LocalDateTime wednesday = LocalDateTime.of(2026, 10, 28, 7, 0);

        automationScheduler.evaluateTimeBasedAutomations(wednesday.withHour(7).withMinute(16));
        assertThat(statusOf(lamp)).isEqualTo(DeviceStatus.OFF);

        automationScheduler.evaluateTimeBasedAutomations(wednesday.plusDays(3).withMinute(15));
        assertThat(statusOf(lamp)).as("samedi, hors daysOfWeek").isEqualTo(DeviceStatus.OFF);

        automationScheduler.evaluateTimeBasedAutomations(wednesday.withMinute(15).withSecond(42));
        assertThat(statusOf(lamp)).isEqualTo(DeviceStatus.ON);
        assertThat(automationRepository.findById(automation.getId()))
                .get().extracting(Automation::getLastTriggeredAt).isNotNull();
    }

    private Device device(String name) {
        Device device = new Device();
        device.setName(name);
        device.setType(DeviceType.LIGHT);
        device.setStatus(DeviceStatus.OFF);
        device.setOnline(true);
        device.setAssignedRoom(room);
        return deviceRepository.save(device);
    }

    private Automation automation(String name) {
        Automation automation = new Automation();
        automation.setName(name);
        automation.setLogic(ConditionLogic.ALL);
        automation.setEnabled(true);
        automation.setCreatedBy(user);
        automation.setHousehold(household);
        return automation;
    }

    private AutomationTrigger stateTrigger(Automation automation, Device device, DeviceStatus status) {
        AutomationTrigger trigger = new AutomationTrigger();
        trigger.setAutomation(automation);
        trigger.setType(TriggerType.DEVICE_STATE_CHANGE);
        trigger.setDevice(device);
        trigger.setStatus(status);
        return trigger;
    }

    private AutomationAction setStatusAction(Automation automation, Device device, DeviceStatus status, int order) {
        AutomationAction action = new AutomationAction();
        action.setAutomation(automation);
        action.setType(ActionType.SET_DEVICE_STATUS);
        action.setDevice(device);
        action.setStatus(status);
        action.setExecutionOrder(order);
        return action;
    }

    private DeviceStateRequest stateRequest(DeviceStatus status) {
        DeviceStateRequest request = new DeviceStateRequest();
        request.setStatus(status);
        return request;
    }

    private DeviceStatus statusOf(Device device) {
        return deviceRepository.findById(device.getId()).orElseThrow().getStatus();
    }
}
