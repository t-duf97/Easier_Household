package be.duft.easier_household.bll.services;

import be.duft.easier_household.TestcontainersConfiguration;
import be.duft.easier_household.api.dtos.request.RoomRequest;
import be.duft.easier_household.api.dtos.response.RoomResponse;
import be.duft.easier_household.bll.exceptions.ResourceAlreadyExistsException;
import be.duft.easier_household.dl.embeddable.HouseholdMemberId;
import be.duft.easier_household.dl.entities.Household;
import be.duft.easier_household.dl.entities.HouseholdMember;
import be.duft.easier_household.dl.entities.User;
import be.duft.easier_household.dl.enums.Role;
import be.duft.easier_household.dl.enums.RoomType;
import be.duft.easier_household.dl.repositories.HouseholdMemberRepository;
import be.duft.easier_household.dl.repositories.HouseholdRepository;
import be.duft.easier_household.dl.repositories.UserRepository;
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
class RoomNameUniquenessIntegrationTest {

    @Autowired private RoomService roomService;
    @Autowired private UserRepository userRepository;
    @Autowired private HouseholdRepository householdRepository;
    @Autowired private HouseholdMemberRepository householdMemberRepository;

    private User user;
    private Household home;
    private Household holidayHome;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setFirstName("Test");
        user.setLastName("Pièces");
        user.setEmail("rooms-" + UUID.randomUUID() + "@test.local");
        user.setPasswordHash("not-a-real-hash");
        user = userRepository.save(user);

        home = household("Maison");
        holidayHome = household("Maison de vacances");
    }

    @Test
    void sameName_isAllowedInTwoHouseholds() {
        roomService.addRoom(request("Cuisine"), home.getId(), user.getId());

        RoomResponse other = roomService.addRoom(request("Cuisine"), holidayHome.getId(), user.getId());

        assertThat(other.getName()).isEqualTo("Cuisine");
    }

    @Test
    void sameName_isRefusedInTheSameHousehold() {
        roomService.addRoom(request("Cuisine"), home.getId(), user.getId());

        assertThatThrownBy(() -> roomService.addRoom(request("Cuisine"), home.getId(), user.getId()))
                .isInstanceOf(ResourceAlreadyExistsException.class);
    }

    @Test
    void rename_isRefusedOntoAnExistingNameButAllowedOntoItsOwn() {
        roomService.addRoom(request("Cuisine"), home.getId(), user.getId());
        RoomResponse bedroom = roomService.addRoom(request("Chambre"), home.getId(), user.getId());

        assertThatThrownBy(() -> roomService.updateRoom(bedroom.getId(), request("Cuisine"), home.getId(), user.getId()))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        RoomResponse unchanged = roomService.updateRoom(bedroom.getId(), request("Chambre"), home.getId(), user.getId());
        assertThat(unchanged.getName()).isEqualTo("Chambre");
    }

    private Household household(String name) {
        Household household = new Household();
        household.setName(name);
        household.setTimeZone(ZoneId.of("Europe/Brussels"));
        household.setCreatedBy(user);
        household = householdRepository.save(household);

        HouseholdMember member = new HouseholdMember();
        member.setHouseholdMemberId(new HouseholdMemberId(user.getId(), household.getId()));
        member.setUser(user);
        member.setHousehold(household);
        member.setRole(Role.OWNER);
        member.setJoinedAt(LocalDateTime.now());
        householdMemberRepository.save(member);
        return household;
    }

    private RoomRequest request(String name) {
        RoomRequest request = new RoomRequest();
        request.setName(name);
        request.setType(RoomType.KITCHEN);
        request.setFloor(0);
        return request;
    }
}
