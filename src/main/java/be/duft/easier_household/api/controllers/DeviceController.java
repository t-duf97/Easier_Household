package be.duft.easier_household.api.controllers;

import be.duft.easier_household.bll.services.DeviceService;
import be.duft.easier_household.dtos.request.DeviceRequest;
import be.duft.easier_household.dtos.response.DeviceResponse;
import be.duft.easier_household.dtos.response.DeviceStateResponse;
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
    /**
     * Pour récupérer tous les devices d'une room.
     * **/
    @GetMapping("/rooms/{roomId}/devices")
    public ResponseEntity<List<DeviceResponse>> getDevices(@PathVariable Long roomId) {
        return ResponseEntity.ok(deviceService.findByRoomId(roomId));
    }

    @PostMapping("/rooms/{roomId}/create")
    public ResponseEntity<DeviceResponse> createDevice(
            @Valid
            @RequestBody DeviceRequest request,
            @PathVariable Long roomId) {
          DeviceResponse response = deviceService.create(request, roomId);
          return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<DeviceResponse> updateDevice(
            @PathVariable Long id,
            @Valid @RequestBody DeviceRequest request) {
        return ResponseEntity.ok(deviceService.update(id, request));
    }

    @DeleteMapping("/devices/{id}")
    public ResponseEntity<DeviceResponse> deleteDevice(@PathVariable Long id){
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/devices/{id}/state")
    public ResponseEntity<DeviceResponse> updateDeviceState(
            @PathVariable Long id,
            @Valid @RequestBody DeviceRequest request){
        log.info("request: {}", request.toString());
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
