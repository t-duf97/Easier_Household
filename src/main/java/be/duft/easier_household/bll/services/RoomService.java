package be.duft.easier_household.bll.services;

import be.duft.easier_household.dl.repositories.HouseholdMemberRepository;
import be.duft.easier_household.dl.repositories.HouseholdRepository;
import be.duft.easier_household.dl.repositories.DeviceRepository;
import be.duft.easier_household.dl.repositories.RoomRepository;
import be.duft.easier_household.dl.entities.Household;
import be.duft.easier_household.dl.entities.HouseholdMember;
import be.duft.easier_household.dl.entities.Room;
import be.duft.easier_household.api.dtos.request.RoomRequest;
import be.duft.easier_household.api.dtos.response.RoomResponse;
import be.duft.easier_household.bll.exceptions.ResourceAlreadyExistsException;
import be.duft.easier_household.bll.exceptions.ResourceNotFoundException;
import be.duft.easier_household.bll.exceptions.UnauthorizedAccessException;
import be.duft.easier_household.bll.mappers.RoomMapper;
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
    private final DeviceRepository deviceRepository;
    private final DeviceService deviceService;
    private final HouseholdRepository householdRepository;
    private final HouseholdMemberRepository householdMemberRepository;

    @Transactional(readOnly = true)
    public RoomResponse findById(Long id, Long userId) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id" + id));
        checkMembership(userId, room.getHousehold().getId());
        return toResponse(room);
    }

//    @Transactional(readOnly = true)
//    public List<RoomResponse> findAll() {
//        return roomRepository.findAll()
//                .stream()
//                .map(this::toResponse)
//                .toList();
//    }

    @Transactional(readOnly = true)
    public List<RoomResponse> findAllByHouseholdId(Long householdId, Long userId) {

        checkMembership(userId, householdId);
        return roomRepository.findAllByHouseholdId(householdId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public RoomResponse addRoom(RoomRequest request, Long householdId, Long userId) {

        Household household = householdRepository.findById(householdId)
                .orElseThrow(() -> new ResourceNotFoundException("Household not found with id" + householdId));

        checkMembership(userId, householdId);
        if (roomRepository.existsByHouseholdIdAndName(householdId, request.getName())) {
            throw new ResourceAlreadyExistsException("A room named '" + request.getName() + "' already exists in this household");
        }

        Room room = roomMapper.FromRequestToEntity(request);
        room.setHousehold(household);
        return toResponse(roomRepository.save(room));
    }

    public RoomResponse updateRoom(Long id, RoomRequest request, Long householdId, Long userId) {

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id" + id));

        checkMembership(userId, householdId);
        if (request.getName() != null
                && roomRepository.existsByHouseholdIdAndNameAndIdNot(householdId, request.getName(), id)) {
            throw new ResourceAlreadyExistsException("A room named '" + request.getName() + "' already exists in this household");
        }

        roomMapper.updateEntityFromRequest(request, room);
        return toResponse(roomRepository.save(room));
    }

    public void deleteRoom(Long id, Long householdId, Long userId) {

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id" + id));

        checkMembership(userId, householdId);

        deviceService.deleteWithReferences(deviceRepository.findByAssignedRoom_Id(id));
        roomRepository.delete(room);
    }

    private void checkMembership(Long userId, Long householdId) {
        HouseholdMember member = householdMemberRepository.findByHouseholdIdAndUserId(householdId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("HouseholdMember not found with id " + userId));

        if (!(member.getHousehold().getId().equals(householdId))) {
            throw new UnauthorizedAccessException("You are not a member of this household.");
        }
    }


    private RoomResponse toResponse(Room room) {
        return roomMapper.FromEntityToResponse(room, deviceRepository.countByAssignedRoom_Id(room.getId()));
    }
}
