package be.duft.easier_household.bll.mappers;

import be.duft.easier_household.dl.entities.Room;
import be.duft.easier_household.api.dtos.request.RoomRequest;
import be.duft.easier_household.api.dtos.response.RoomResponse;
import org.springframework.stereotype.Component;

@Component
public class RoomMapper {

    public RoomResponse FromEntityToResponse(Room room, int deviceCount){

        RoomResponse response = new RoomResponse();
        response.setId(room.getId());
        response.setName(room.getName());
        response.setCreatedAt(room.getCreatedAt());
        response.setFloor(room.getFloor());
        response.setHouseholdId(room.getHousehold().getId());
        response.setType(room.getRoomType());
        response.setDeviceCount(deviceCount);
        return response;
    }

    public Room FromRequestToEntity(RoomRequest roomRequest){
        Room room = new Room();
        room.setName(roomRequest.getName());
        room.setFloor(roomRequest.getFloor());
        room.setRoomType(roomRequest.getType());
        return room;
    }

    public void updateEntityFromRequest(RoomRequest request, Room room){
        if (request.getFloor() != null){
            room.setFloor(request.getFloor());
        }
        if (request.getType() != null){
            room.setRoomType(request.getType());
        }
        if (request.getName() != null){
            room.setName(request.getName());
        }
    }
}
