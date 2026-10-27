package be.duft.easier_household.api.controllers;

import be.duft.easier_household.bll.services.AutomationService;
import be.duft.easier_household.api.dtos.request.AutomationRequest;
import be.duft.easier_household.api.dtos.response.AutomationResponse;
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
public class AutomationController extends BaseController {

    private final AutomationService automationService;


    @PostMapping("/households/{householdId}/automations")
    public ResponseEntity<AutomationResponse> createAutomation(
            @PathVariable Long householdId,
            @Valid @RequestBody AutomationRequest request
    ){
        AutomationResponse automation = automationService.create(householdId, request, getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(automation);
    }

    @GetMapping("/households/{householdId}/automations")
    public ResponseEntity<List<AutomationResponse>> getAllAutomations(@PathVariable Long householdId){
        List<AutomationResponse> automations = automationService.findByHousehold(householdId, getCurrentUserId());
        return ResponseEntity.status(HttpStatus.OK).body(automations);
    }

    @GetMapping("/automations/{id}")
    public ResponseEntity<AutomationResponse> getAutomation(@PathVariable Long id){
        AutomationResponse automation = automationService.findById(id, getCurrentUserId());
        return ResponseEntity.status(HttpStatus.OK).body(automation);
    }

    @PatchMapping("/automations/{id}/toggle")
    public ResponseEntity<AutomationResponse> toggleAutomation(@PathVariable Long id){
        AutomationResponse automation = automationService.toggleEnabled(id, getCurrentUserId());
        return ResponseEntity.status(HttpStatus.OK).body(automation);
    }

    @PutMapping("/automations/{id}")
    public ResponseEntity<AutomationResponse> updateAutomation(
            @PathVariable Long id,
            @Valid @RequestBody AutomationRequest request
    ) {
        AutomationResponse automation = automationService.update(id, request, getCurrentUserId());
        return ResponseEntity.ok(automation);
    }

    @DeleteMapping("/automations/{id}")
    public ResponseEntity<AutomationResponse> deleteAutomation(@PathVariable Long id){
        automationService.delete(id, getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

}
