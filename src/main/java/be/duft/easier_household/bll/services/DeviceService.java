package be.duft.easier_household.bll.services;


import be.duft.easier_household.dl.repositories.DeviceRepository;
import be.duft.easier_household.dl.repositories.DeviceStateRepository;
import be.duft.easier_household.dl.repositories.RoomRepository;
import be.duft.easier_household.dl.entities.Device;
import be.duft.easier_household.dl.entities.DeviceState;
import be.duft.easier_household.dl.entities.Room;
import be.duft.easier_household.api.dtos.request.DeviceRequest;
import be.duft.easier_household.api.dtos.request.DeviceStateRequest;
import be.duft.easier_household.api.dtos.response.DeviceResponse;
import be.duft.easier_household.api.dtos.response.DeviceStateResponse;
import be.duft.easier_household.bll.exceptions.DeviceUnavailableException;
import be.duft.easier_household.bll.exceptions.ResourceNotFoundException;
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
    private final DeviceMapper deviceMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public DeviceResponse findById(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Device not found with id " + id));
        return deviceMapper.fromEntityToResponse(device);
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> findByRoomId(Long roomId) {
        return deviceRepository.findByAssignedRoom_Id(roomId)
                .stream()
                .map(deviceMapper::fromEntityToResponse)
                .collect(Collectors.toList());
    }

    public DeviceResponse create(DeviceRequest request, Long roomId){
        Room room = roomRepository.findById(roomId)
                .orElseThrow(()-> new ResourceNotFoundException("Room not found with id " + roomId));
        Device device = deviceMapper.FromRequestToEntity(request, room);
        deviceRepository.save(device);
        roomRepository.save(room);
        return deviceMapper.fromEntityToResponse(device, roomId);
    }

    public DeviceResponse update(Long id, DeviceRequest request) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found: " + id));

        if (request.getName() != null) {
            device.setName(request.getName());
        }
        if (request.getType() != null) {
            device.setType(request.getType());
        }

        device.setUpdatedAt(LocalDateTime.now());
        return deviceMapper.fromEntityToResponse(deviceRepository.save(device));
    }


    public void deleteDevice(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found: " + id));

        deviceStateRepository.deleteByDeviceId(id);
        deviceRepository.delete(device);
    }

    public DeviceResponse changeState(Long id, DeviceStateRequest request){

        Device device = deviceRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Device not found with id " + id));
        if (!device.isOnline()){
            throw new DeviceUnavailableException("Device is unavailable");
        }
        DeviceStatus previousStatus = device.getStatus();

        DeviceState state = new DeviceState();
        state.setChangedAt(LocalDateTime.now());
        state.setDevice(device);
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
    public Page<DeviceStateResponse> getHistory(Long deviceId, Pageable pageable) {
        return deviceStateRepository.findByDeviceIdOrderByChangedAtDesc(deviceId, pageable)
                .map(deviceMapper::toStateResponse);
    }
}
