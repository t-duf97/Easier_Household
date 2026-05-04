package be.duft.easier_household.bll.services;


import be.duft.easier_household.dal.repositories.DeviceRepository;
import be.duft.easier_household.dal.repositories.DeviceStateRepository;
import be.duft.easier_household.dal.repositories.RoomRepository;
import be.duft.easier_household.dl.entities.Device;
import be.duft.easier_household.dtos.responseDTO.DeviceResponse;
import be.duft.easier_household.exceptions.ResourceNotFoundException;
import be.duft.easier_household.mappers.DeviceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

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

}
