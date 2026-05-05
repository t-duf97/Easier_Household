package be.duft.easier_household.api.controllers;

import be.duft.easier_household.bll.services.DeviceService;
import be.duft.easier_household.dtos.responseDTO.DeviceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/devices")
@Validated
public class DeviceController {

    private final DeviceService deviceService;

    /**
     * Pour récupérer les informations d'un device en particulier, via son ID.
     * **/
    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponse> getDevice(@PathVariable Long id) {
        return ResponseEntity.ok(deviceService.findById(id));
    }
    /**
     * Pour récupérer tous les devices d'une room.
     * **/
    @GetMapping("/rooms/{roomId}/devices")
    public ResponseEntity<List<DeviceResponse>> getDevices(@PathVariable Long roomId) {
        return ResponseEntity.ok(deviceService.findByRoomId(roomId));
    }



}
