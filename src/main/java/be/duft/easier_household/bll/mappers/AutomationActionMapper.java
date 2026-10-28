package be.duft.easier_household.bll.mappers;

import be.duft.easier_household.dl.repositories.DeviceRepository;
import be.duft.easier_household.dl.entities.AutomationAction;
import be.duft.easier_household.api.dtos.request.AutomationActionRequest;
import be.duft.easier_household.api.dtos.response.AutomationActionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AutomationActionMapper {

    private final DeviceRepository deviceRepository;

    public AutomationActionResponse fromEntityToResponse (AutomationAction action){
        AutomationActionResponse response = new AutomationActionResponse();

        response.setId(action.getId());
        response.setExecutionOrder(action.getExecutionOrder());
        response.setType(action.getType());
        response.setTargetValue(action.getTargetValue());
        response.setStatus(action.getStatus());
        response.setNotificationMsg(action.getNotificationMsg());
        response.setDelayInSeconds(action.getDelayInSeconds());

        // Device optionnel : SEND_NOTIFICATION et WAIT n'en ont pas.
        if (action.getDevice() != null) {
            response.setDeviceId(action.getDevice().getId());
        }
        return response;

    }

    public AutomationAction fromRequestToEntity(AutomationActionRequest request){
        AutomationAction action = new AutomationAction();

        action.setExecutionOrder(request.getExecutionOrder() != null ? request.getExecutionOrder() : 1);
        action.setType(request.getType());
        action.setTargetValue(request.getTargetValue());
        action.setNotificationMsg(request.getNotificationMsg());
        action.setDelayInSeconds(request.getDelayInSeconds() != null ? request.getDelayInSeconds() : 0);
        action.setStatus(request.getStatus());

        if (request.getDeviceId() != null) {
            deviceRepository.findById(request.getDeviceId())
                    .ifPresent(action::setDevice);
        }

        return action;

    }
}
