package be.duft.easier_household.bll.mappers;

import be.duft.easier_household.dl.entities.AutomationTrigger;
import be.duft.easier_household.dl.entities.Device;
import be.duft.easier_household.api.dtos.request.AutomationTriggerRequest;
import be.duft.easier_household.api.dtos.response.AutomationTriggerResponse;
import org.springframework.stereotype.Component;

@Component
public class AutomationTriggerMapper {

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

        if (trigger.getAutomation() != null) {
            response.setAutomationId(trigger.getAutomation().getId());
        }
        if (trigger.getDevice() != null){
            response.setDeviceId(trigger.getDevice().getId());
            response.setDeviceName(trigger.getDevice().getName());
        }
        return response;
    }

    /**
     * @param device le device référencé par {@code request.deviceId}, déjà résolu par le service (null si aucun)
     */
    public AutomationTrigger fromRequestToEntity(AutomationTriggerRequest request, Device device) {
        AutomationTrigger trigger = new AutomationTrigger();

        trigger.setType(request.getType());
        trigger.setTimeValue(request.getTimeValue());
        trigger.setCronExpression(request.getCronExpression());
        trigger.setDaysOfWeek(request.getDaysOfWeek());
        trigger.setStatus(request.getStatus());
        trigger.setThresholdValue(request.getThresholdValue());
        trigger.setThresholdOperator(request.getThresholdOperator());
        trigger.setDevice(device);
        return trigger;
    }
}
