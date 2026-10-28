package be.duft.easier_household.bll.mappers;

import be.duft.easier_household.dl.repositories.DeviceRepository;
import be.duft.easier_household.dl.entities.AutomationCondition;
import be.duft.easier_household.dl.entities.Device;
import be.duft.easier_household.api.dtos.request.AutomationConditionRequest;
import be.duft.easier_household.api.dtos.response.AutomationConditionResponse;
import be.duft.easier_household.bll.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AutomationConditionMapper {

    private final DeviceRepository deviceRepository;

    public AutomationConditionResponse fromEntityToResponse(AutomationCondition condition) {
        AutomationConditionResponse response = new AutomationConditionResponse();

        response.setId(condition.getId());
        response.setType(condition.getType());
        response.setStatus(condition.getStatus());
        response.setThresholdValue(condition.getThresholdValue());
        response.setThresholdOperator(condition.getThresholdOperator());
        response.setTimeFrom(condition.getTimeFrom());
        response.setTimeTo(condition.getTimeTo());
        response.setDaysOfWeek(condition.getDaysOfWeek());
        response.setDurationInMinutes(condition.getDurationInMinutes());

        // Device optionnel : TIME_RANGE et DAY_OF_WEEK n'en ont pas.
        if (condition.getDevice() != null) {
            response.setDeviceId(condition.getDevice().getId());
            response.setDeviceName(condition.getDevice().getName());
        }
        return response;
    }

    public AutomationCondition fromRequestToEntity(AutomationConditionRequest request) {
        AutomationCondition condition = new AutomationCondition();

        condition.setType(request.getType());
        condition.setThresholdValue(request.getThresholdValue());
        condition.setThresholdOperator(request.getThresholdOperator());
        condition.setTimeFrom(request.getTimeFrom());
        condition.setTimeTo(request.getTimeTo());
        condition.setDaysOfWeek(request.getDaysOfWeek());
        condition.setDurationInMinutes(request.getDurationInMinutes());
        condition.setStatus(request.getStatus());

        if (request.getDeviceId() != null) {
            Device device = deviceRepository.findById(request.getDeviceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Device with id: " + request.getDeviceId() + " not found!"));

            condition.setDevice(device);
        }
        return condition;
    }

}
