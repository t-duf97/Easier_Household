package be.duft.easier_household.bll.services;

import be.duft.easier_household.dl.repositories.HouseholdMemberRepository;
import be.duft.easier_household.dl.repositories.HouseholdRepository;
import be.duft.easier_household.dl.repositories.RoomRepository;
import be.duft.easier_household.dl.embeddable.HouseholdMemberId;
import be.duft.easier_household.dl.entities.Household;
import be.duft.easier_household.dl.entities.HouseholdMember;
import be.duft.easier_household.dl.entities.Room;
import be.duft.easier_household.dl.entities.User;
import be.duft.easier_household.dl.enums.Role;
import be.duft.easier_household.api.dtos.request.HouseholdRequest;
import be.duft.easier_household.api.dtos.response.HouseholdResponse;
import be.duft.easier_household.bll.exceptions.ResourceNotFoundException;
import be.duft.easier_household.bll.exceptions.UnauthorizedAccessException;
import be.duft.easier_household.bll.mappers.HouseholdMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HouseholdService {

    private final HouseholdRepository householdRepository;
    private final RoomRepository roomRepository;
    private final HouseholdMemberRepository householdMemberRepository;
    private final HouseholdMapper householdMapper;
    private final UserService userService;

    public HouseholdResponse findById(Long id) {
        Household household = householdRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Household not found: " + id));

        Set<HouseholdMember> members = householdMemberRepository.findByHousehold(household);
        List<Room> rooms = roomRepository.findAllByHouseholdId(household.getId());
        return householdMapper.fromEntityToResponse(household, members, rooms);
    }

    public List<HouseholdResponse> findAllByUserId(Long userId) {
        return householdMemberRepository.findByUserId(userId)
                .stream()
                .map(member -> householdMapper.fromEntityToResponse(member.getHousehold()))
                .collect(Collectors.toList());
    }

    public HouseholdResponse create(HouseholdRequest request, Long userId) {
        User user = userService.findById(userId);

        Household household = householdMapper.fromRequesttoEntity(request);
        household.setCreatedBy(user);

        // 1. Sauvegarder le household EN PREMIER pour qu'il ait un id
        Household savedHousehold = householdRepository.save(household);

        HouseholdMemberId memberId = new HouseholdMemberId(userId, savedHousehold.getId());

        HouseholdMember householdMember = new HouseholdMember();
        householdMember.setHouseholdMemberId(memberId);
        householdMember.setHousehold(savedHousehold);
        householdMember.setUser(user);
        householdMember.setRole(Role.OWNER);
        householdMember.setJoinedAt(LocalDateTime.now());

        householdMemberRepository.save(householdMember);

        return householdMapper.fromEntityToResponse(savedHousehold);
    }

    public HouseholdResponse update(HouseholdRequest request, Long userId, Long id) {
        Household household = householdRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Household not found: " + id));

        checkOwnership(household, userId);

        householdMapper.updateEntityFromRequest(request, household);
        return householdMapper.fromEntityToResponse(householdRepository.save(household));

    }

    public void delete(Long id, Long userId) {
        Household household = householdRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Household not found: " + id));

        checkOwnership(household, userId);

        householdRepository.delete(household);
    }

    private void checkOwnership(Household household, Long userId) {
        HouseholdMember householdMember = householdMemberRepository
                .findByHouseholdIdAndUserId(household.getId(), userId)
                .orElseThrow(()-> new UnauthorizedAccessException("Not a member of this house!"));

        if (!(householdMember.getRole().equals(Role.OWNER))) {
            throw new UnauthorizedAccessException("You need to be the owner to do that.");
        }
    }
}
