package be.duft.easier_household.api.controllers;

import be.duft.easier_household.bll.services.AutomationService;
import be.duft.easier_household.dtos.request.AutomationRequest;
import be.duft.easier_household.dtos.response.AutomationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class AutomationController {

    private final AutomationService automationService;


    @PostMapping("/households/{householdId}/automations")
    public ResponseEntity<AutomationResponse> createAutomation(
            @PathVariable Long householdId,
            @Valid @RequestBody AutomationRequest request,
            Long userId
    ){
        AutomationResponse automation = automationService.create(householdId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(automation);
    }

    @GetMapping("/households/{householdId}/automations")
    public ResponseEntity<List<AutomationResponse>> getAllAutomations(
            @PathVariable Long householdId,
            Long userId
    ){
        List<AutomationResponse> automations = automationService.findByHousehold(householdId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(automations);
    }

    @GetMapping("/automations/{id}")
    public ResponseEntity<AutomationResponse> getAutomation(
            @PathVariable Long id,
            Long userId
    ){
        AutomationResponse automation = automationService.findById(id, userId);
        return ResponseEntity.status(HttpStatus.OK).body(automation);
    }

    @PatchMapping("/automations/{id}/toggle")
    public ResponseEntity<AutomationResponse> toggleAutomation(
            @PathVariable Long id,
            Long userId
    ){
        AutomationResponse automation = automationService.toggleEnabled(id, userId);
        return ResponseEntity.status(HttpStatus.OK).body(automation);
    }

    @DeleteMapping("/automations/{id}")
    public ResponseEntity<AutomationResponse> deleteAutomation(
            @PathVariable Long id,
            Long userId
    ){
        automationService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }


}
