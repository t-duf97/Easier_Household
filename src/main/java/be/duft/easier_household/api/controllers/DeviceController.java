package be.duft.easier_household.api.controllers;

import be.duft.easier_household.bll.services.DeviceService;
import be.duft.easier_household.api.dtos.request.DeviceRequest;
import be.duft.easier_household.api.dtos.request.DeviceStateRequest;
import be.duft.easier_household.api.dtos.response.DeviceResponse;
import be.duft.easier_household.api.dtos.response.DeviceStateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
public class DeviceController extends BaseController {

    private final DeviceService deviceService;

    /**
     * Pour récupérer les informations d'un device en particulier, via son ID.
     * **/
    @GetMapping("devices/{id}")
    public ResponseEntity<DeviceResponse> getDevice(@PathVariable Long id) {
        return ResponseEntity.ok(deviceService.findById(id));
    }

    @GetMapping("/rooms/{roomId}/devices")
    public ResponseEntity<List<DeviceResponse>> getDevices(@PathVariable Long roomId) {
        return ResponseEntity.ok(deviceService.findByRoomId(roomId));
    }

    @PostMapping("/rooms/{roomId}/devices")
    public ResponseEntity<DeviceResponse> createDevice(
            @Valid
            @RequestBody DeviceRequest request,
            @PathVariable Long roomId) {
          DeviceResponse response = deviceService.create(request, roomId);
          return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/devices/{id}")
    public ResponseEntity<DeviceResponse> updateDevice(
            @PathVariable Long id,
            @Valid @RequestBody DeviceRequest request) {
        return ResponseEntity.ok(deviceService.update(id, request));
    }

    @DeleteMapping("/devices/{id}")
    public ResponseEntity<DeviceResponse> deleteDevice(@PathVariable Long id){
        deviceService.deleteDevice(id, getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/devices/{id}/state")
    public ResponseEntity<DeviceResponse> updateDeviceState(
            @PathVariable Long id,
            @Valid @RequestBody DeviceStateRequest request){
        return ResponseEntity.ok(deviceService.changeState(id, request));
    }

    @GetMapping("devices/{id}/history")
    public ResponseEntity<Page<DeviceStateResponse>>  getDeviceHistory(
            @PathVariable Long id,
            @Valid @RequestParam(defaultValue = "0") int page,
            @Valid @RequestParam(defaultValue = "20") int pageSize
    ){

        return ResponseEntity.ok(deviceService.getHistory(id, PageRequest.of(page, pageSize)));
    }


}
