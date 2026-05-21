package be.duft.easier_household.bll.services;

import be.duft.easier_household.dal.repositories.HouseholdMemberRepository;
import be.duft.easier_household.dal.repositories.HouseholdRepository;
import be.duft.easier_household.dal.repositories.RoomRepository;
import be.duft.easier_household.dl.entities.Household;
import be.duft.easier_household.dl.entities.HouseholdMember;
import be.duft.easier_household.dl.entities.Room;
import be.duft.easier_household.dtos.request.RoomRequest;
import be.duft.easier_household.dtos.response.RoomResponse;
import be.duft.easier_household.exceptions.ResourceNotFoundException;
import be.duft.easier_household.exceptions.UnauthorizedAccessException;
import be.duft.easier_household.mappers.RoomMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private final HouseholdRepository householdRepository;
    private final HouseholdMemberRepository householdMemberRepository;

    @Transactional(readOnly = true)
    public RoomResponse findById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id" + id));
        return roomMapper.FromEntityToResponse(room);
    }

//    @Transactional(readOnly = true)
//    public List<RoomResponse> findAll() {
//        return roomRepository.findAll()
//                .stream()
//                .map(roomMapper::FromEntityToResponse)
//                .toList();
//    }

    @Transactional(readOnly = true)
    public List<RoomResponse> findAllByHouseholdId(Long householdId, Long userId) {

        checkMembership(householdId, userId);

        return roomRepository.findAll()
                .stream()
                .map(room -> roomMapper.FromEntityToResponse(roomRepository.findByHouseholdId(householdId)))
                .collect(Collectors.toList());
    }

    public RoomResponse addRoom(RoomRequest request, Long householdId, Long userId) {

        Household household = householdRepository.findById(householdId)
                .orElseThrow(() -> new ResourceNotFoundException("Household not found with id" + householdId));

        checkMembership(userId, householdId);

        Room room = roomMapper.FromRequestToEntity(request);
        room.setHousehold(household);
        return roomMapper.FromEntityToResponse(roomRepository.save(room));
    }

    public RoomResponse updateRoom(Long id, RoomRequest request, Long householdId, Long userId) {

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id" + id));

        checkMembership(userId, householdId);

        roomMapper.updateEntityFromRequest(request, room);
        return roomMapper.FromEntityToResponse(roomRepository.save(room));
    }

    public void deleteRoom(Long id, Long householdId, Long userId) {

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id" + id));

        checkMembership(userId, householdId);

        roomRepository.delete(room);
    }

    private void checkMembership(Long userId, Long householdId) {
        HouseholdMember member = householdMemberRepository.findByHouseholdIdAndUserId(householdId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("HouseholdMember not found with id" + userId));

        if (!(member.getHousehold().getId().equals(householdId))) {
            throw new UnauthorizedAccessException("You are not a member of this household.");
        }
    }

}
