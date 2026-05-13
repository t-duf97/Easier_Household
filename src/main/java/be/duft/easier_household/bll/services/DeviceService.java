package be.duft.easier_household.bll.services;


import be.duft.easier_household.dal.repositories.DeviceRepository;
import be.duft.easier_household.dal.repositories.DeviceStateRepository;
import be.duft.easier_household.dal.repositories.RoomRepository;
import be.duft.easier_household.dl.entities.Device;
import be.duft.easier_household.dl.entities.DeviceState;
import be.duft.easier_household.dl.entities.Room;
import be.duft.easier_household.dtos.requestDTO.DeviceRequest;
import be.duft.easier_household.dtos.responseDTO.DeviceResponse;
import be.duft.easier_household.dtos.responseDTO.DeviceStateResponse;
import be.duft.easier_household.exceptions.DeviceUnavailableException;
import be.duft.easier_household.exceptions.ResourceNotFoundException;
import be.duft.easier_household.mappers.DeviceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final RoomRepository roomRepository;
    private final DeviceStateRepository deviceStateRepository;
    private final DeviceMapper deviceMapper;

    @Transactional(readOnly = true)
    public DeviceResponse findById(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Device not found with id " + id));
        return deviceMapper.fromEntityToResponse(device);
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> findByRoomId(Long roomId) {
        return deviceRepository.findByRoomId(roomId)
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

    public DeviceResponse update(Long id, DeviceRequest request){
        Device device = deviceRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Device not found with id " + id));

        deviceMapper.updateEntityFromRequest(request, device);
        return deviceMapper.fromEntityToResponse(deviceRepository.save(device));
    }

    public void deleteDevice(Long id){
        deviceRepository.deleteById(id);
    }

    public DeviceResponse changeState(Long id, DeviceRequest request){
        Device device = deviceRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Device not found with id " + id));
        if (!device.isOnline()){
            throw new DeviceUnavailableException("Device is unavailable");
        }

        DeviceState state = new DeviceState();
        state.setDevice(device);
        state.setStatus(request.getStatus());
        state.setValue(request.getValue());
        state.setChangeSource(request.getSource());
        deviceStateRepository.save(state);

        device.setStatus(request.getStatus());
        device.setCurrentValue(request.getValue());
        device.setUpdatedAt(LocalDateTime.now());
        deviceRepository.save(device);

        return deviceMapper.fromEntityToResponse(device);

    }

    @Transactional(readOnly = true)
    public Page<DeviceStateResponse> getHistory(Long deviceId, Pageable pageable) {
        return deviceStateRepository.findByDeviceIdOrderByChangedAtDesc(deviceId, pageable)
                .map(deviceMapper::toStateResponse);
    }
}
