package be.duft.easier_household.api.controllers;

import be.duft.easier_household.bll.services.HouseholdService;
import be.duft.easier_household.dtos.request.HouseholdRequest;
import be.duft.easier_household.dtos.response.HouseholdResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/households")
public class HouseholdController {
    private final HouseholdService householdService;

    @GetMapping("/{id}")
    public ResponseEntity<HouseholdResponse> getHousehold(@PathVariable Long id) {
        return ResponseEntity.ok(householdService.findById(id));
    }

    @GetMapping("/display/{id}")
    public ResponseEntity<List<HouseholdResponse>> getAllHouseholds(@PathVariable Long id) {
        return ResponseEntity.ok(householdService.findAllByUserId(id));
    }

    // POST /households/create --> Create new household
    @PostMapping("/create")
    public ResponseEntity<HouseholdResponse> createHousehold(@Valid @RequestBody HouseholdRequest request, Long userId) {
        HouseholdResponse response = householdService.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HouseholdResponse> updateHousehold(@Valid @RequestBody HouseholdRequest request,
                                                             Long userId,
                                                             @PathVariable Long id) {
        HouseholdResponse response = householdService.update(request, userId, id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<HouseholdResponse> deleteHousehold(@PathVariable Long id, Long userId) {

        householdService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
