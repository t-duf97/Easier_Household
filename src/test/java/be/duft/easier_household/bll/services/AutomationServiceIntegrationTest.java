package be.duft.easier_household.bll.services;

import be.duft.easier_household.TestcontainersConfiguration;
import be.duft.easier_household.api.dtos.request.AutomationActionRequest;
import be.duft.easier_household.api.dtos.request.AutomationRequest;
import be.duft.easier_household.api.dtos.request.AutomationTriggerRequest;
import be.duft.easier_household.api.dtos.response.AutomationActionResponse;
import be.duft.easier_household.api.dtos.response.AutomationResponse;
import be.duft.easier_household.bll.exceptions.InvalidRequestException;
import be.duft.easier_household.bll.exceptions.ResourceNotFoundException;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "jwt.secret=dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLW9ubHktbm90LXJlYWwtMTIzNDU2")
@Import(TestcontainersConfiguration.class)
class AutomationServiceIntegrationTest {

    @Autowired private AutomationService automationService;
    @Autowired private UserRepository userRepository;
    @Autowired private HouseholdRepository householdRepository;
    @Autowired private HouseholdMemberRepository householdMemberRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private DeviceRepository deviceRepository;
    @Autowired private AutomationRepository automationRepository;

    private User owner;
    private Household household;
    private Device lamp;
    private Device neighboursLamp;

    @BeforeEach
    void setUp() {
        owner = user();
        household = household(owner);
        lamp = device(room(household));

        User neighbour = user();
        neighboursLamp = device(room(household(neighbour)));
    }

    @Test
    void create_refusesDeviceFromAnotherHousehold() {
        AutomationRequest request = request(trigger(lamp.getId()),
                List.of(statusAction(neighboursLamp.getId(), null)));

        assertThatThrownBy(() -> automationService.create(household.getId(), request, owner.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(automationRepository.findByHouseholdId(household.getId())).isEmpty();
    }

    @Test
    void create_numbersActionsByPositionAndFillsReadableFields() {
        AutomationActionRequest notify = new AutomationActionRequest();
        notify.setType(ActionType.SEND_NOTIFICATION);
        notify.setNotificationMsg("allumée");

        AutomationResponse response = automationService.create(household.getId(),
                request(trigger(lamp.getId()), List.of(statusAction(lamp.getId(), null), notify)), owner.getId());

        assertThat(response.getActions())
                .extracting(AutomationActionResponse::getExecutionOrder)
                .containsExactly(1, 2);
        assertThat(response.getActions().getFirst().getDeviceName()).isEqualTo(lamp.getName());
        assertThat(response.getActions().getFirst().getStatus()).isEqualTo(DeviceStatus.OFF);
        assertThat(response.getActions()).allSatisfy(a -> assertThat(a.getAutomationId()).isEqualTo(response.getId()));
        assertThat(response.getTriggers().getFirst().getDeviceName()).isEqualTo(lamp.getName());
    }

    @Test
    void create_refusesMissingOrIncompleteTriggers() {
        AutomationRequest noTrigger = request(trigger(lamp.getId()), List.of(statusAction(lamp.getId(), null)));
        noTrigger.setTriggers(List.of());
        assertThatThrownBy(() -> automationService.create(household.getId(), noTrigger, owner.getId()))
                .isInstanceOf(InvalidRequestException.class);

        AutomationTriggerRequest withoutStatus = trigger(lamp.getId());
        withoutStatus.setStatus(null);
        assertThatThrownBy(() -> automationService.create(household.getId(),
                request(withoutStatus, List.of(statusAction(lamp.getId(), null))), owner.getId()))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("status");
    }

    @Test
    void update_cannotRemoveAllTriggers() {
        AutomationResponse created = automationService.create(household.getId(),
                request(trigger(lamp.getId()), List.of(statusAction(lamp.getId(), null))), owner.getId());

        AutomationRequest emptied = request(trigger(lamp.getId()), List.of(statusAction(lamp.getId(), null)));
        emptied.setTriggers(List.of());

        assertThatThrownBy(() -> automationService.update(created.getId(), emptied, owner.getId()))
                .isInstanceOf(InvalidRequestException.class);
        assertThat(automationService.findById(created.getId(), owner.getId()).getTriggers()).hasSize(1);
    }

    private AutomationRequest request(AutomationTriggerRequest trigger, List<AutomationActionRequest> actions) {
        AutomationRequest request = new AutomationRequest();
        request.setName("Test");
        request.setTriggers(List.of(trigger));
        request.setActions(actions);
        return request;
    }

    private AutomationTriggerRequest trigger(Long deviceId) {
        AutomationTriggerRequest trigger = new AutomationTriggerRequest();
        trigger.setType(TriggerType.DEVICE_STATE_CHANGE);
        trigger.setStatus(DeviceStatus.ON);
        trigger.setDeviceId(deviceId);
        return trigger;
    }

    private AutomationActionRequest statusAction(Long deviceId, Integer order) {
        AutomationActionRequest action = new AutomationActionRequest();
        action.setType(ActionType.SET_DEVICE_STATUS);
        action.setStatus(DeviceStatus.OFF);
        action.setDeviceId(deviceId);
        action.setExecutionOrder(order);
        return action;
    }

    private User user() {
        User user = new User();
        user.setFirstName("Test");
        user.setLastName("Service");
        user.setEmail("service-" + UUID.randomUUID() + "@test.local");
        user.setPasswordHash("not-a-real-hash");
        return userRepository.save(user);
    }

    private Household household(User owner) {
        Household household = new Household();
        household.setName("Foyer");
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
        return household;
    }

    private Room room(Household household) {
        Room room = new Room();
        room.setName("Pièce-" + UUID.randomUUID());
        room.setFloor(0);
        room.setRoomType(RoomType.OTHER);
        room.setHousehold(household);
        return roomRepository.save(room);
    }

    private Device device(Room room) {
        Device device = new Device();
        device.setName("Lampe " + room.getName());
        device.setType(DeviceType.LIGHT);
        device.setStatus(DeviceStatus.OFF);
        device.setOnline(true);
        device.setAssignedRoom(room);
        return deviceRepository.save(device);
    }
}
