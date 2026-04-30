package be.duft.easier_household.api.controllers;

import be.duft.easier_household.bll.services.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;
}
