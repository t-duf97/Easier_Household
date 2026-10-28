package be.duft.easier_household.bll.services;

import be.duft.easier_household.TestcontainersConfiguration;
import be.duft.easier_household.bll.exceptions.UnauthorizedAccessException;
import be.duft.easier_household.dl.embeddable.HouseholdMemberId;
import be.duft.easier_household.dl.entities.*;
import be.duft.easier_household.dl.enums.*;
import be.duft.easier_household.dl.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "jwt.secret=dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLW9ubHktbm90LXJlYWwtMTIzNDU2")
@Import(TestcontainersConfiguration.class)
class DeviceDeletionIntegrationTest {

    @Autowired private DeviceService deviceService;
    @Autowired private RoomService roomService;
    @Autowired private UserRepository userRepository;
    @Autowired private HouseholdRepository householdRepository;
    @Autowired private HouseholdMemberRepository householdMemberRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private DeviceRepository deviceRepository;
    @Autowired private DeviceStateRepository deviceStateRepository;
    @Autowired private AutomationRepository automationRepository;

    private User owner;
    private Household household;
    private Room kitchen;
    private Room bedroom;
    private Device sensor;
    private Device kitchenLamp;
    private Device bedroomLamp;
    private Automation usesSensorAsCondition;
    private Automation independent;

    @BeforeEach
    void setUp() {
        owner = user();
        household = new Household();
        household.setName("Maison");
        household.setTimeZone(ZoneId.of("Europe/Brussels"));
        household.setCreatedBy(owner);
        household = householdRepository.save(household);
        HouseholdMember member = new HouseholdMember();
        member.setHouseholdMemberId(new HouseholdMemberId(owner.getId(), household.getId()));
        member.setUser(owner);
        member.setHousehold(household);
        member.setRole(Role.OWNER);
        member.setJoinedAt(LocalDateTime.now());
        householdMemberRepository.save(member);

        kitchen = room("Cuisine");
        bedroom = room("Chambre");
        sensor = device(kitchen, DeviceType.SENSOR_MOTION);
        kitchenLamp = device(kitchen, DeviceType.LIGHT);
        bedroomLamp = device(bedroom, DeviceType.LIGHT);

        DeviceState state = new DeviceState();
        state.setDevice(sensor);
        state.setStatus(DeviceStatus.OFF);
        state.setChangeSource(ChangeSource.MANUAL);
        state.setChangedAt(LocalDateTime.now());
        deviceStateRepository.save(state);

        usesSensorAsCondition = automation("Lampe chambre ON si mouvement cuisine");
        usesSensorAsCondition.getTriggers().add(stateTrigger(usesSensorAsCondition, bedroomLamp));
        AutomationCondition motion = new AutomationCondition();
        motion.setAutomation(usesSensorAsCondition);
        motion.setType(ConditionType.DEVICE_STATUS);
        motion.setDevice(sensor);
        motion.setStatus(DeviceStatus.ON);
        usesSensorAsCondition.getConditions().add(motion);
        usesSensorAsCondition.getActions().add(notification(usesSensorAsCondition));
        usesSensorAsCondition = automationRepository.save(usesSensorAsCondition);

        independent = automation("Lampe chambre ON -> notification");
        independent.getTriggers().add(stateTrigger(independent, bedroomLamp));
        independent.getActions().add(notification(independent));
        independent = automationRepository.save(independent);
    }

    @Test
    void deleteDevice_removesAutomationsReferencingItAndKeepsTheOthers() {
        deviceService.deleteDevice(sensor.getId(), owner.getId());

        assertThat(deviceRepository.findById(sensor.getId())).isEmpty();
        assertThat(deviceStateRepository.existsByDeviceIdAndChangedAtAfter(sensor.getId(), LocalDateTime.now().minusYears(1)))
                .isFalse();
        assertThat(automationRepository.findById(usesSensorAsCondition.getId()))
                .as("une condition retirée rendrait l'automatisation moins restrictive : elle est supprimée")
                .isEmpty();
        assertThat(automationRepository.findById(independent.getId())).isPresent();
        assertThat(deviceRepository.findById(kitchenLamp.getId())).isPresent();
    }

    @Test
    void deleteDevice_isRefusedForNonMember() {
        User stranger = user();

        assertThatThrownBy(() -> deviceService.deleteDevice(sensor.getId(), stranger.getId()))
                .isInstanceOf(UnauthorizedAccessException.class);
        assertThat(deviceRepository.findById(sensor.getId())).isPresent();
    }

    @Test
    void deleteRoom_removesItsDevicesAndTheirAutomations() {
        roomService.deleteRoom(kitchen.getId(), household.getId(), owner.getId());

        assertThat(roomRepository.findById(kitchen.getId())).isEmpty();
        assertThat(deviceRepository.findById(sensor.getId())).isEmpty();
        assertThat(deviceRepository.findById(kitchenLamp.getId())).isEmpty();
        assertThat(automationRepository.findById(usesSensorAsCondition.getId())).isEmpty();
        assertThat(automationRepository.findById(independent.getId())).isPresent();
        assertThat(roomRepository.findById(bedroom.getId())).isPresent();
    }

    private User user() {
        User user = new User();
        user.setFirstName("Test");
        user.setLastName("Suppression");
        user.setEmail("deletion-" + UUID.randomUUID() + "@test.local");
        user.setPasswordHash("not-a-real-hash");
        return userRepository.save(user);
    }

    private Room room(String name) {
        Room room = new Room();
        room.setName(name + "-" + UUID.randomUUID());
        room.setFloor(0);
        room.setRoomType(RoomType.OTHER);
        room.setHousehold(household);
        return roomRepository.save(room);
    }

    private Device device(Room room, DeviceType type) {
        Device device = new Device();
        device.setName(type + " " + room.getName());
        device.setType(type);
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
        automation.setCreatedBy(owner);
        automation.setHousehold(household);
        return automation;
    }

    private AutomationTrigger stateTrigger(Automation automation, Device device) {
        AutomationTrigger trigger = new AutomationTrigger();
        trigger.setAutomation(automation);
        trigger.setType(TriggerType.DEVICE_STATE_CHANGE);
        trigger.setDevice(device);
        trigger.setStatus(DeviceStatus.ON);
        return trigger;
    }

    private AutomationAction notification(Automation automation) {
        AutomationAction action = new AutomationAction();
        action.setAutomation(automation);
        action.setType(ActionType.SEND_NOTIFICATION);
        action.setNotificationMsg("test");
        action.setExecutionOrder(1);
        return action;
    }
}
