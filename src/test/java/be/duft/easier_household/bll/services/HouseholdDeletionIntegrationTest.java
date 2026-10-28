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
class HouseholdDeletionIntegrationTest {

    @Autowired private HouseholdService householdService;
    @Autowired private UserRepository userRepository;
    @Autowired private HouseholdRepository householdRepository;
    @Autowired private HouseholdMemberRepository householdMemberRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private DeviceRepository deviceRepository;
    @Autowired private DeviceStateRepository deviceStateRepository;
    @Autowired private AutomationRepository automationRepository;

    private User owner;
    private User resident;
    private Household household;
    private Room room;
    private Device lamp;
    private Automation automation;

    @BeforeEach
    void setUp() {
        owner = user("owner");
        resident = user("resident");

        household = new Household();
        household.setName("À supprimer");
        household.setTimeZone(ZoneId.of("Europe/Brussels"));
        household.setCreatedBy(owner);
        household = householdRepository.save(household);
        member(owner, Role.OWNER);
        member(resident, Role.RESIDENT);

        room = new Room();
        room.setName("Cuisine-" + UUID.randomUUID());
        room.setFloor(0);
        room.setRoomType(RoomType.KITCHEN);
        room.setHousehold(household);
        room = roomRepository.save(room);

        lamp = new Device();
        lamp.setName("Lampe");
        lamp.setType(DeviceType.LIGHT);
        lamp.setStatus(DeviceStatus.ON);
        lamp.setOnline(true);
        lamp.setAssignedRoom(room);
        lamp = deviceRepository.save(lamp);

        DeviceState state = new DeviceState();
        state.setDevice(lamp);
        state.setStatus(DeviceStatus.ON);
        state.setChangeSource(ChangeSource.MANUAL);
        state.setChangedAt(LocalDateTime.now());
        deviceStateRepository.save(state);

        automation = new Automation();
        automation.setName("Lampe ON -> notification");
        automation.setLogic(ConditionLogic.ALL);
        automation.setEnabled(true);
        automation.setCreatedBy(owner);
        automation.setHousehold(household);
        AutomationTrigger trigger = new AutomationTrigger();
        trigger.setAutomation(automation);
        trigger.setType(TriggerType.DEVICE_STATE_CHANGE);
        trigger.setDevice(lamp);
        trigger.setStatus(DeviceStatus.ON);
        automation.getTriggers().add(trigger);
        AutomationAction action = new AutomationAction();
        action.setAutomation(automation);
        action.setType(ActionType.SET_DEVICE_STATUS);
        action.setDevice(lamp);
        action.setStatus(DeviceStatus.OFF);
        action.setExecutionOrder(1);
        automation.getActions().add(action);
        automation = automationRepository.save(automation);
    }

    @Test
    void owner_deletesHouseholdWithEverythingThatDependsOnIt() {
        householdService.delete(household.getId(), owner.getId());

        assertThat(householdRepository.findById(household.getId())).isEmpty();
        assertThat(householdMemberRepository.findByUserId(owner.getId())).isEmpty();
        assertThat(roomRepository.findById(room.getId())).isEmpty();
        assertThat(deviceRepository.findById(lamp.getId())).isEmpty();
        assertThat(deviceStateRepository.existsByDeviceIdAndChangedAtAfter(lamp.getId(), LocalDateTime.now().minusYears(1))).isFalse();
        assertThat(automationRepository.findById(automation.getId())).isEmpty();
        assertThat(userRepository.findById(owner.getId())).isPresent();
    }

    @Test
    void resident_cannotDeleteHousehold() {
        assertThatThrownBy(() -> householdService.delete(household.getId(), resident.getId()))
                .isInstanceOf(UnauthorizedAccessException.class);

        assertThat(householdRepository.findById(household.getId())).isPresent();
        assertThat(deviceRepository.findById(lamp.getId())).isPresent();
    }

    private User user(String role) {
        User user = new User();
        user.setFirstName(role);
        user.setLastName("Test");
        user.setEmail(role + "-" + UUID.randomUUID() + "@test.local");
        user.setPasswordHash("not-a-real-hash");
        return userRepository.save(user);
    }

    private void member(User user, Role role) {
        HouseholdMember member = new HouseholdMember();
        member.setHouseholdMemberId(new HouseholdMemberId(user.getId(), household.getId()));
        member.setUser(user);
        member.setHousehold(household);
        member.setRole(role);
        member.setJoinedAt(LocalDateTime.now());
        householdMemberRepository.save(member);
    }
}
