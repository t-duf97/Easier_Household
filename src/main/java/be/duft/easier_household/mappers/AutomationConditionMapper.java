package be.duft.easier_household.mappers;

import be.duft.easier_household.dal.repositories.DeviceRepository;
import be.duft.easier_household.dl.entities.AutomationCondition;
import be.duft.easier_household.dl.entities.Device;
import be.duft.easier_household.dtos.requestDTO.AutomationConditionRequest;
import be.duft.easier_household.dtos.responseDTO.AutomationConditionResponse;
import be.duft.easier_household.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AutomationConditionMapper {

    private final DeviceRepository deviceRepository;

    public AutomationConditionResponse fromEntityToResponse(AutomationCondition condition) {
        AutomationConditionResponse response = new AutomationConditionResponse();

        response.setId(condition.getId());
        response.setConditionType(condition.getConditionType());
        response.setDeviceId(condition.getDevice().getId());
        response.setDeviceName(condition.getDevice().getName());
        response.setDeviceStatus(condition.getDevice().getStatus());
        response.setThresholdValue(condition.getThresholdValue());
        response.setThresholdOperator(condition.getThresholdOperator());
        response.setTimeFrom(condition.getTimeFrom());
        response.setTimeTo(condition.getTimeTo());
        response.setDaysOfWeek(condition.getDaysOfWeek());
        response.setDurationInMinutes(condition.getDurationInMinutes());

        return response;
    }

    public AutomationCondition fromRequestToEntity(AutomationConditionRequest request) {
        AutomationCondition condition = new AutomationCondition();

        condition.setConditionType(request.getConditionType());
        condition.setThresholdValue(request.getThresholdValue());
        condition.setThresholdOperator(request.getThresholdOperator());
        condition.setTimeFrom(request.getTimeFrom());
        condition.setTimeTo(request.getTimeTo());
        condition.setDaysOfWeek(request.getDaysOfWeek());
        condition.setDurationInMinutes(request.getDurationInMinutes());

        if (request.getDeviceId() != null) {
            Device device = deviceRepository.findById(request.getDeviceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Device with id: " + request.getDeviceId() + " not found!"));

            condition.setDevice(device);
            condition.setExpectedDeviceStatus(request.getExpectedDeviceStatus());

        }
        return condition;
    }

}
