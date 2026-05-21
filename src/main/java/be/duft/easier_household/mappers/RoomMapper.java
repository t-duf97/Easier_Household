package be.duft.easier_household.mappers;

import be.duft.easier_household.dl.entities.Room;
import be.duft.easier_household.dtos.request.RoomRequest;
import be.duft.easier_household.dtos.response.RoomResponse;
import org.springframework.stereotype.Component;

@Component
public class RoomMapper {

    public RoomResponse FromEntityToResponse(Room room){

        RoomResponse response = new RoomResponse();
        response.setId(room.getId());
        response.setName(room.getName());
        response.setCreated_at(room.getCreatedAt());
        response.setFloor(room.getFloor());
        response.setHouseholdId(room.getHousehold().getId());
        response.setRoomType(room.getRoomType());
        return response;
    }

    public Room FromRequestToEntity(RoomRequest roomRequest){
        Room room = new Room();
        room.setName(roomRequest.getName());
        room.setFloor(roomRequest.getFloor());
        room.setRoomType(roomRequest.getRoomType());
        return room;
    }

    public void updateEntityFromRequest(RoomRequest request, Room room){
        if (request.getFloor() != null){
            room.setFloor(request.getFloor());
        }
        if (request.getRoomType() != null){
            room.setRoomType(request.getRoomType());
        }
        if (request.getName() != null){
            room.setName(request.getName());
        }
    }
}
