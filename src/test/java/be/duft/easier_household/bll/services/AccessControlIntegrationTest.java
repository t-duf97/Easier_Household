package be.duft.easier_household.bll.services;

import be.duft.easier_household.TestcontainersConfiguration;
import be.duft.easier_household.api.dtos.request.DeviceRequest;
import be.duft.easier_household.api.dtos.request.DeviceStateRequest;
import be.duft.easier_household.bll.exceptions.ResourceNotFoundException;
import be.duft.easier_household.bll.exceptions.UnauthorizedAccessException;
import be.duft.easier_household.dl.embeddable.HouseholdMemberId;
import be.duft.easier_household.dl.entities.*;
import be.duft.easier_household.dl.enums.*;
import be.duft.easier_household.dl.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Un utilisateur qui n'est pas membre du foyer ne peut ni lire ni modifier ses pièces et devices.
 */
@SpringBootTest(properties = "jwt.secret=dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLW9ubHktbm90LXJlYWwtMTIzNDU2")
@Import(TestcontainersConfiguration.class)
class AccessControlIntegrationTest {

    @Autowired private DeviceService deviceService;
    @Autowired private RoomService roomService;
    @Autowired private HouseholdService householdService;
    @Autowired private UserRepository userRepository;
    @Autowired private HouseholdRepository householdRepository;
    @Autowired private HouseholdMemberRepository householdMemberRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private DeviceRepository deviceRepository;
    @Autowired private DeviceStateRepository deviceStateRepository;

    private User member;
    private User stranger;
    private Household household;
    private Room room;
    private Device lamp;

    @BeforeEach
    void setUp() {
        member = user();
        stranger = user();

        household = new Household();
        household.setName("Maison privée");
        household.setTimeZone(ZoneId.of("Europe/Brussels"));
        household.setCreatedBy(member);
        household = householdRepository.save(household);
        HouseholdMember membership = new HouseholdMember();
        membership.setHouseholdMemberId(new HouseholdMemberId(member.getId(), household.getId()));
        membership.setUser(member);
        membership.setHousehold(household);
        membership.setRole(Role.RESIDENT);
        membership.setJoinedAt(LocalDateTime.now());
        householdMemberRepository.save(membership);

        room = new Room();
        room.setName("Salon-" + UUID.randomUUID());
        room.setFloor(0);
        room.setRoomType(RoomType.LIVING_ROOM);
        room.setHousehold(household);
        room = roomRepository.save(room);

        lamp = new Device();
        lamp.setName("Lampe");
        lamp.setType(DeviceType.LIGHT);
        lamp.setStatus(DeviceStatus.OFF);
        lamp.setOnline(true);
        lamp.setAssignedRoom(room);
        lamp = deviceRepository.save(lamp);
    }

    @Test
    void stranger_isRefusedEverywhere() {
        Long id = stranger.getId();
        refused(() -> deviceService.findById(lamp.getId(), id));
        refused(() -> deviceService.findByRoomId(room.getId(), id));
        refused(() -> deviceService.create(deviceRequest("Intrus"), room.getId(), id));
        refused(() -> deviceService.update(lamp.getId(), deviceRequest("Renommée"), id));
        refused(() -> deviceService.changeState(lamp.getId(), on(), id));
        refused(() -> deviceService.getHistory(lamp.getId(), Pageable.unpaged(), id));
        refused(() -> roomService.findById(room.getId(), id));
        refused(() -> householdService.findById(household.getId(), id));

        Device unchanged = deviceRepository.findById(lamp.getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(DeviceStatus.OFF);
        assertThat(unchanged.getName()).isEqualTo("Lampe");
        assertThat(deviceRepository.findByAssignedRoom_Id(room.getId())).hasSize(1);
    }

    @Test
    void member_canUseDevicesAndIsRecordedAsAuthorOfTheChange() {
        assertThat(deviceService.findById(lamp.getId(), member.getId()).getName()).isEqualTo("Lampe");
        assertThat(roomService.findById(room.getId(), member.getId()).getId()).isEqualTo(room.getId());
        assertThat(householdService.findById(household.getId(), member.getId()).getId()).isEqualTo(household.getId());

        deviceService.changeState(lamp.getId(), on(), member.getId());

        assertThat(deviceStateRepository.findByDeviceIdOrderByChangedAtDesc(lamp.getId(), Pageable.unpaged()))
                .singleElement()
                .satisfies(state -> assertThat(state.getChangedBy()).isEqualTo(member.getId()));
    }

    private void refused(ThrowingSupplier<?> call) {
        assertThatThrownBy(call::get)
                .isInstanceOfAny(UnauthorizedAccessException.class, ResourceNotFoundException.class);
    }

    private DeviceRequest deviceRequest(String name) {
        DeviceRequest request = new DeviceRequest();
        request.setName(name);
        request.setType(DeviceType.LIGHT);
        return request;
    }

    private DeviceStateRequest on() {
        DeviceStateRequest request = new DeviceStateRequest();
        request.setStatus(DeviceStatus.ON);
        return request;
    }

    private User user() {
        User user = new User();
        user.setFirstName("Test");
        user.setLastName("Accès");
        user.setEmail("access-" + UUID.randomUUID() + "@test.local");
        user.setPasswordHash("not-a-real-hash");
        return userRepository.save(user);
    }
}
