package be.duft.easier_household.bll.services;


import be.duft.easier_household.dl.repositories.AutomationRepository;
import be.duft.easier_household.dl.repositories.DeviceIntegrationRepository;
import be.duft.easier_household.dl.repositories.DeviceRepository;
import be.duft.easier_household.dl.repositories.DeviceStateRepository;
import be.duft.easier_household.dl.repositories.HouseholdMemberRepository;
import be.duft.easier_household.dl.repositories.RoomRepository;
import be.duft.easier_household.dl.entities.Automation;
import be.duft.easier_household.dl.entities.Device;
import be.duft.easier_household.dl.entities.DeviceState;
import be.duft.easier_household.dl.entities.HouseholdMember;
import be.duft.easier_household.dl.entities.Room;
import be.duft.easier_household.api.dtos.request.DeviceRequest;
import be.duft.easier_household.api.dtos.request.DeviceStateRequest;
import be.duft.easier_household.api.dtos.response.DeviceResponse;
import be.duft.easier_household.api.dtos.response.DeviceStateResponse;
import be.duft.easier_household.bll.exceptions.DeviceUnavailableException;
import be.duft.easier_household.bll.exceptions.ResourceNotFoundException;
import be.duft.easier_household.bll.exceptions.UnauthorizedAccessException;
import be.duft.easier_household.bll.events.DeviceStatusChangedEvent;
import be.duft.easier_household.bll.mappers.DeviceMapper;
import be.duft.easier_household.dl.enums.DeviceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final RoomRepository roomRepository;
    private final DeviceStateRepository deviceStateRepository;
    private final DeviceIntegrationRepository deviceIntegrationRepository;
    private final AutomationRepository automationRepository;
    private final HouseholdMemberRepository householdMemberRepository;
    private final DeviceMapper deviceMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public DeviceResponse findById(Long id, Long userId) {
        return deviceMapper.fromEntityToResponse(accessibleDevice(id, userId));
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> findByRoomId(Long roomId, Long userId) {
        accessibleRoom(roomId, userId);
        return deviceRepository.findByAssignedRoom_Id(roomId)
                .stream()
                .map(deviceMapper::fromEntityToResponse)
                .collect(Collectors.toList());
    }

    public DeviceResponse create(DeviceRequest request, Long roomId, Long userId){
        Room room = accessibleRoom(roomId, userId);
        Device device = deviceMapper.FromRequestToEntity(request, room);
        deviceRepository.save(device);
        roomRepository.save(room);
        return deviceMapper.fromEntityToResponse(device, roomId);
    }

    public DeviceResponse update(Long id, DeviceRequest request, Long userId) {
        Device device = accessibleDevice(id, userId);

        if (request.getName() != null) {
            device.setName(request.getName());
        }
        if (request.getType() != null) {
            device.setType(request.getType());
        }

        device.setUpdatedAt(LocalDateTime.now());
        return deviceMapper.fromEntityToResponse(deviceRepository.save(device));
    }


    public void deleteDevice(Long id, Long userId) {
        deleteWithReferences(List.of(accessibleDevice(id, userId)));
    }

    /**
     * Supprime les devices avec leur historique, leur intégration et <b>toute automatisation</b> qui en référence un
     * (trigger, condition ou action). L'automatisation entière est supprimée : retirer seulement une condition
     * la rendrait moins restrictive à l'insu de l'utilisateur.
     */
    public void deleteWithReferences(List<Device> devices) {
        if (devices.isEmpty()) {
            return;
        }
        List<Long> deviceIds = devices.stream().map(Device::getId).toList();

        List<Automation> automations = automationRepository.findReferencingDevices(deviceIds);
        automations.forEach(a -> log.info("Deleting automation '{}' (id={}) : it references a deleted device",
                a.getName(), a.getId()));
        automationRepository.deleteAll(automations);
        deviceStateRepository.deleteByDeviceIdIn(deviceIds);
        deviceIntegrationRepository.deleteByDeviceIdIn(deviceIds);
        deviceRepository.deleteAll(devices);
    }

    public DeviceResponse changeState(Long id, DeviceStateRequest request, Long userId){

        Device device = deviceRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Device not found with id " + id));
        HouseholdMember member = checkMembership(householdIdOf(device), userId);
        if (!device.isOnline()){
            throw new DeviceUnavailableException("Device is unavailable");
        }
        DeviceStatus previousStatus = device.getStatus();

        DeviceState state = new DeviceState();
        state.setChangedAt(LocalDateTime.now());
        state.setDevice(device);
        state.setUser(member.getUser());
        if (request.getStatus() != null) {
        state.setStatus(request.getStatus());} else {state.setStatus(device.getStatus());}
        if (request.getValue() != null) {
            state.setValue(request.getValue());
        } else { state.setValue(device.getCurrentValue());}
        if (request.getSource() != null) {
        state.setChangeSource(request.getSource());
        }
        deviceStateRepository.save(state);


        if (request.getStatus() != null) {
            device.setStatus(request.getStatus());
        }
        if (request.getValue() != null) {
            device.setCurrentValue(request.getValue());
        }
        device.setUpdatedAt(LocalDateTime.now());
        deviceRepository.save(device);

        if (device.getStatus() != previousStatus) {
            eventPublisher.publishEvent(new DeviceStatusChangedEvent(device.getId(), device.getStatus()));
        }

        return deviceMapper.fromEntityToResponse(device);

    }

    @Transactional(readOnly = true)
    public Page<DeviceStateResponse> getHistory(Long deviceId, Pageable pageable, Long userId) {
        accessibleDevice(deviceId, userId);
        return deviceStateRepository.findByDeviceIdOrderByChangedAtDesc(deviceId, pageable)
                .map(deviceMapper::toStateResponse);
    }

    /**
     * @throws ResourceNotFoundException si le device n'existe pas
     * @throws UnauthorizedAccessException si l'utilisateur n'est pas membre du foyer du device
     */
    private Device accessibleDevice(Long id, Long userId) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found: " + id));
        checkMembership(householdIdOf(device), userId);
        return device;
    }

    /**
     * @throws ResourceNotFoundException si la pièce n'existe pas
     * @throws UnauthorizedAccessException si l'utilisateur n'est pas membre du foyer de la pièce
     */
    private Room accessibleRoom(Long roomId, Long userId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id " + roomId));
        checkMembership(room.getHousehold().getId(), userId);
        return room;
    }

    private Long householdIdOf(Device device) {
        return device.getAssignedRoom().getHousehold().getId();
    }

    private HouseholdMember checkMembership(Long householdId, Long userId) {
        return householdMemberRepository.findByHouseholdIdAndUserId(householdId, userId)
                .orElseThrow(() -> new UnauthorizedAccessException("User is not a member of this household"));
    }
}
