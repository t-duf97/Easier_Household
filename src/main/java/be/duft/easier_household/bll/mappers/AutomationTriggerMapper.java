package be.duft.easier_household.bll.mappers;

import be.duft.easier_household.dl.repositories.DeviceRepository;
import be.duft.easier_household.dl.entities.AutomationTrigger;
import be.duft.easier_household.dl.entities.Device;
import be.duft.easier_household.api.dtos.request.AutomationTriggerRequest;
import be.duft.easier_household.api.dtos.response.AutomationTriggerResponse;
import be.duft.easier_household.bll.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AutomationTriggerMapper {

    private final DeviceRepository deviceRepository;

    public AutomationTriggerResponse fromEntityToResponse(AutomationTrigger trigger) {
        AutomationTriggerResponse response = new AutomationTriggerResponse();

        response.setId(trigger.getId());
        response.setType(trigger.getType());
        response.setTimeValue(trigger.getTimeValue());
        response.setThresholdValue(trigger.getThresholdValue());
        response.setThresholdOperator(trigger.getThresholdOperator());
        response.setCronExpression(trigger.getCronExpression());
        response.setStatus(trigger.getStatus());
        response.setDaysOfWeek(trigger.getDaysOfWeek());

        if (trigger.getDevice() != null){
            response.setDeviceId(trigger.getDevice().getId());
            response.setDeviceName(trigger.getDevice().getName());
        }
        return response;
    }

    public AutomationTrigger fromRequestToEntity(AutomationTriggerRequest request) {
        AutomationTrigger trigger = new AutomationTrigger();

        trigger.setType(request.getType());
        trigger.setTimeValue(request.getTimeValue());
        trigger.setCronExpression(request.getCronExpression());
        trigger.setDaysOfWeek(request.getDaysOfWeek());
        trigger.setStatus(request.getStatus());
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
