package be.duft.easier_household.api.controllers;

import be.duft.easier_household.bll.services.HouseholdService;
import be.duft.easier_household.api.dtos.request.HouseholdRequest;
import be.duft.easier_household.api.dtos.response.HouseholdResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/households")
public class HouseholdController extends BaseController {
    private final HouseholdService householdService;

    @GetMapping("/{id}")
    public ResponseEntity<HouseholdResponse> getHousehold(@PathVariable Long id) {
        return ResponseEntity.ok(householdService.findById(id, getCurrentUserId()));
    }

    @GetMapping()
    public ResponseEntity<List<HouseholdResponse>> getAllHouseholds() {
        return ResponseEntity.ok(householdService.findAllByUserId(getCurrentUserId()));
    }

    @PostMapping()
    public ResponseEntity<HouseholdResponse> createHousehold(@Valid @RequestBody HouseholdRequest request) {
        HouseholdResponse response = householdService.create(request, getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HouseholdResponse> updateHousehold(@Valid @RequestBody HouseholdRequest request,
                                                             @PathVariable Long id) {
        HouseholdResponse response = householdService.update(request, getCurrentUserId(), id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<HouseholdResponse> deleteHousehold(@PathVariable Long id) {

        householdService.delete(id, getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
