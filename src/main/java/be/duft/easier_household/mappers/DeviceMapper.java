package be.duft.easier_household.mappers;

import be.duft.easier_household.dl.entities.Device;
import be.duft.easier_household.dl.entities.Room;
import be.duft.easier_household.dl.enums.DeviceType;
import be.duft.easier_household.dtos.requestDTO.DeviceRequest;
import be.duft.easier_household.dtos.responseDTO.DeviceResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DeviceMapper {

    public DeviceResponse fromEntityToResponse(Device device) {
        DeviceResponse response = new DeviceResponse();
        response.setId(device.getId());
        response.setName(device.getName());
        response.setAssignedRoom(device.getAssignedRoom());
        response.setOnline(device.isOnline());
        response.setCreatedAt(device.getCreatedAt());
        response.setUpdatedAt(device.getUpdatedAt());
        response.setDeviceType(device.getDeviceType());
        response.setRoomId(device.getRoomId());
        response.setDeviceStatus(device.getStatus());
        response.setCurrentValue(device.getCurrentValue());
        return response;
    }

    public Device FromRequestToEntity(DeviceRequest request) {
        Device device = new Device();
        device.setName(request.getName());
        device.setAssignedRoom(request.getAssignedRoom());
        device.setDeviceType(request.getDeviceType());
        return device;
    }

    // Le toEntity est peut etre mauvais. je ne sais pas si je récupère tout ce dont
    //j'aurai besoin dans le formulaire, mais j'ai déjà un premier pdv.
}




