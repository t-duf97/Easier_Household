package be.duft.easier_household.bll.mappers;

import be.duft.easier_household.dl.entities.AutomationAction;
import be.duft.easier_household.dl.entities.Device;
import be.duft.easier_household.api.dtos.request.AutomationActionRequest;
import be.duft.easier_household.api.dtos.response.AutomationActionResponse;
import org.springframework.stereotype.Component;

@Component
public class AutomationActionMapper {

    public AutomationActionResponse fromEntityToResponse (AutomationAction action){
        AutomationActionResponse response = new AutomationActionResponse();

        response.setId(action.getId());
        response.setExecutionOrder(action.getExecutionOrder());
        response.setType(action.getType());
        response.setTargetValue(action.getTargetValue());
        response.setStatus(action.getStatus());
        response.setNotificationMsg(action.getNotificationMsg());
        response.setDelayInSeconds(action.getDelayInSeconds());

        if (action.getAutomation() != null) {
            response.setAutomationId(action.getAutomation().getId());
        }
        // Device optionnel : SEND_NOTIFICATION et WAIT n'en ont pas.
        if (action.getDevice() != null) {
            response.setDeviceId(action.getDevice().getId());
            response.setDeviceName(action.getDevice().getName());
        }
        return response;

    }

    /**
     * @param device le device référencé par {@code request.deviceId}, déjà résolu par le service (null si aucun)
     * @return une action dont l'executionOrder vaut 0 s'il n'est pas renseigné (le service lui attribue sa position)
     */
    public AutomationAction fromRequestToEntity(AutomationActionRequest request, Device device){
        AutomationAction action = new AutomationAction();

        action.setExecutionOrder(request.getExecutionOrder() != null ? request.getExecutionOrder() : 0);
        action.setType(request.getType());
        action.setTargetValue(request.getTargetValue());
        action.setNotificationMsg(request.getNotificationMsg());
        action.setDelayInSeconds(request.getDelayInSeconds() != null ? request.getDelayInSeconds() : 0);
        action.setStatus(request.getStatus());
        action.setDevice(device);
        return action;

    }
}
