package be.duft.easier_household.api.controllers;

import be.duft.easier_household.bll.services.HouseholdService;
import be.duft.easier_household.bll.services.RoomService;
import be.duft.easier_household.dtos.request.RoomRequest;
import be.duft.easier_household.dtos.response.RoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
//@RequestMapping("/rooms")
public class RoomController extends BaseController {

    private final RoomService roomService;
    private final HouseholdService householdService;

    @GetMapping("/rooms/{id}")
    public ResponseEntity<RoomResponse> getRoom(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.findById(id));
    }

    @PutMapping("/rooms/{id}")
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable Long id, @RequestBody RoomRequest roomRequest) {
        Long householdId = roomService.findById(id).getHouseholdId();

        RoomResponse roomResponse = roomService.updateRoom(id, roomRequest, householdId, getCurrentUserId());
        return ResponseEntity.ok(roomResponse);
    }

    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<RoomResponse> deleteRoom(@PathVariable Long id) {
        Long householdId = roomService.findById(id).getHouseholdId();
        roomService.deleteRoom(id, householdId, getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/households/{householdId}/rooms")
    public ResponseEntity<RoomResponse> createRoom(@PathVariable Long householdId,
                                                   @RequestBody RoomRequest roomRequest){
        RoomResponse response = roomService.addRoom(roomRequest, householdId, getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/households/{householdId}/rooms")
    public ResponseEntity<List<RoomResponse>> getRoomsByHousehold(@PathVariable Long householdId) {

        return ResponseEntity.ok(roomService.findAllByHouseholdId(householdId, getCurrentUserId()));
    }
}
