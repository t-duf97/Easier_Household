package be.duft.easier_household.mappers;

import be.duft.easier_household.dal.repositories.DeviceRepository;
import be.duft.easier_household.dl.entities.AutomationAction;
import be.duft.easier_household.dtos.request.AutomationActionRequest;
import be.duft.easier_household.dtos.response.AutomationActionResponse;
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
        response.setActionType(action.getActionType());
        response.setTargetValue(action.getTargetValue());
        response.setDeviceId(action.getDeviceId());
        response.setDeviceStatus(action.getDevice().getStatus());
        response.setNotificationMsg(action.getNotificationMsg());
        response.setDelayInSeconds(action.getDelayInSeconds());

        return response;

    }

    public AutomationAction fromRequestToEntity(AutomationActionRequest request){
        AutomationAction action = new AutomationAction();

        action.setExecutionOrder(request.getExecutionOrder());
        action.setActionType(request.getActionType());
        action.setTargetValue(request.getTargetValue());
        action.setNotificationMsg(request.getNotificationMsg());
        action.setDelayInSeconds(request.getDelayInSeconds());
        action.setTargetStatus(request.getTargetStatus());

        if (request.getDeviceId() != null) {
            action.setDeviceId(request.getDeviceId());
        }

        return action;

    }
}
