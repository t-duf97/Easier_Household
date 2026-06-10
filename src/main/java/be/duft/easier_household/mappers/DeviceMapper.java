package be.duft.easier_household.mappers;

import be.duft.easier_household.dl.entities.Device;
import be.duft.easier_household.dl.entities.DeviceState;
import be.duft.easier_household.dl.entities.Room;
import be.duft.easier_household.dl.enums.DeviceStatus;
import be.duft.easier_household.dtos.request.DeviceRequest;
import be.duft.easier_household.dtos.response.DeviceResponse;
import be.duft.easier_household.dtos.response.DeviceStateResponse;
import org.springframework.stereotype.Component;

@Component
public class DeviceMapper {

    public DeviceResponse fromEntityToResponse(Device device) {
        DeviceResponse response = new DeviceResponse();
        response.setId(device.getId());
        response.setName(device.getName());
        response.setOnline(device.isOnline());
        response.setCreatedAt(device.getCreatedAt());
        response.setUpdatedAt(device.getUpdatedAt());
        response.setDeviceType(device.getDeviceType());
        response.setDeviceStatus(device.getStatus());
        response.setCurrentValue(device.getCurrentValue());
        // Ajouter les infos dénormalisées
        if (device.getAssignedRoom() != null) {
            response.setRoomId(device.getAssignedRoom().getId());
            response.setRoomName(device.getAssignedRoom().getName());
            if (device.getAssignedRoom().getHousehold() != null) {
                response.setHouseholdName(device.getAssignedRoom().getHousehold().getName());
            }
        }
        return response;
    }

    public DeviceResponse fromEntityToResponse(Device device, Long roomId) {
        DeviceResponse response = fromEntityToResponse(device);
        response.setRoomId(roomId);
        return response;
    }

    public Device FromRequestToEntity(DeviceRequest request, Room room) {
        Device device = new Device();
        device.setName(request.getName());
        device.setAssignedRoom(room);
        device.setDeviceType(request.getDeviceType());
        device.setStatus(DeviceStatus.OFF);
        device.setOnline(true);
        return device;
    }

    public void updateEntityFromRequest(DeviceRequest request, Device device) {
        device.setName(request.getName());
        device.setDeviceType(request.getDeviceType());
    }

    public DeviceStateResponse toStateResponse(DeviceState state) {

        DeviceStateResponse response = new DeviceStateResponse();
        response.setId(state.getId());
        response.setDeviceId(state.getDevice().getId());
        response.setValue(state.getValue());
        response.setChangeSource(state.getChangeSource());
        response.setChangedAt(state.getChangedAt());
        return response;
}

    // Le toEntity est peut etre mauvais. je ne sais pas si je récupère tout ce dont
    //j'aurai besoin dans le formulaire, mais j'ai déjà un premier pdv.
}




