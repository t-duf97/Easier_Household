package be.duft.easier_household.mappers;

import be.duft.easier_household.dal.repositories.DeviceRepository;
import be.duft.easier_household.dl.entities.AutomationTrigger;
import be.duft.easier_household.dl.entities.Device;
import be.duft.easier_household.dtos.request.AutomationTriggerRequest;
import be.duft.easier_household.dtos.response.AutomationTriggerResponse;
import be.duft.easier_household.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AutomationTriggerMapper {

    private final DeviceRepository deviceRepository;

    public AutomationTriggerResponse fromEntityToResponse(AutomationTrigger trigger) {
        AutomationTriggerResponse response = new AutomationTriggerResponse();

        response.setId(trigger.getId());
        response.setTriggerType(trigger.getTriggerType());
        response.setTimeValue(trigger.getTimeValue());
        response.setThresholdValue(trigger.getThresholdValue());
        response.setThresholdOperator(trigger.getThresholdOperator());
        response.setCronExpression(trigger.getCronExpression());
        response.setExpectedStatus(trigger.getExpectedStatus());
        response.setDaysOfWeek(trigger.getDaysOfWeek());

        if (trigger.getDevice() != null){
            response.setDeviceId(trigger.getDevice().getId());
            response.setDeviceName(trigger.getDevice().getName());
        }
        return response;
    }

    public AutomationTrigger fromRequestToEntity(AutomationTriggerRequest request) {
        AutomationTrigger trigger = new AutomationTrigger();

        trigger.setTriggerType(request.getTriggerType());
        trigger.setTimeValue(request.getTimeValue());
        trigger.setCronExpression(request.getCronExpression());
        trigger.setDaysOfWeek(request.getDaysOfWeek());
        trigger.setExpectedStatus(request.getExpectedStatus());
        trigger.setThresholdValue(request.getThresholdValue());
        trigger.setThresholdOperator(request.getThresholdOperator());

        if (request.getDeviceId()!= null){
            Device device = deviceRepository.findById(request.getDeviceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Device with id: " + request.getDeviceId() + " not found!"));
            trigger.setDevice(device);
        }
        return trigger;
    }
}
