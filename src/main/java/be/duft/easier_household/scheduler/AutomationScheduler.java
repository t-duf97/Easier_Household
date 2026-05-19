package be.duft.easier_household.scheduler;

import be.duft.easier_household.dal.repositories.AutomationRepository;
import be.duft.easier_household.dal.repositories.DeviceRepository;
import be.duft.easier_household.dal.repositories.DeviceStateRepository;
import be.duft.easier_household.dl.entities.Automation;
import be.duft.easier_household.dl.entities.AutomationAction;
import be.duft.easier_household.dl.entities.Device;
import be.duft.easier_household.dl.entities.DeviceState;
import be.duft.easier_household.dl.enums.ChangeSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutomationScheduler {

    private final AutomationRepository automationRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceStateRepository deviceStateRepository;


    public void executeAction(AutomationAction action, Automation automation) {
        switch (action.getActionType()){
            case SET_DEVICE_STATUS -> {
                if (action.getDevice() == null) return;
                Device device = deviceRepository.findById(action.getDevice().getId()).orElse(null);
                if (device == null) return;

                device.setStatus(action.getTargetStatus());
                device.setUpdatedAt(LocalDateTime.now());
                deviceRepository.save(device);

                DeviceState deviceState = new DeviceState();
                deviceState.setDevice(device);
                deviceState.setValue(action.getTargetValue());
                deviceState.setChangeSource(ChangeSource.AUTOMATION);
                deviceState.setChangedAt(LocalDateTime.now());
                deviceStateRepository.save(deviceState);

                log.info("Action executed : Device '{}' status set to {} by automation '{}'",
                        device.getName(), action.getTargetStatus(), automation.getName());
            }
            case SET_DEVICE_VALUE -> {
                if (action.getDevice() == null) return;
                Device device = deviceRepository.findById(action.getDevice().getId()).orElse(null);
                if (device == null) return;

                device.setCurrentValue(action.getTargetValue());
                device.setUpdatedAt(LocalDateTime.now());
                deviceRepository.save(device);

                DeviceState deviceState = deviceStateRepository.findById(action.getDevice().getId()).orElse(null);
                if (deviceState == null) return;
                deviceState.setValue(action.getTargetValue());
                deviceState.setChangeSource(ChangeSource.AUTOMATION);
                deviceState.setChangedAt(LocalDateTime.now());
                deviceStateRepository.save(deviceState);

                log.info("Action executed : Device '{}' value set to {} by automation '{}'",
                        device.getName(), action.getTargetValue(), automation.getName());
            }
            case SEND_NOTIFICATION -> {
                if (action.getDevice() == null) return;
                log.info("[NOTIFICATION from '{}'] {}", automation.getName(), action.getNotificationMsg());
            }

            case WAIT -> {
                if (action.getDelayInSeconds() > 0){
                    log.debug("Automation '{}' waiting {} seconds", automation.getName(), action.getDelayInSeconds());
                try {
                    Thread.sleep(action.getDelayInSeconds() * 1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Automation wait interrupted for '{}'", automation.getName());
                }
                }
            }
        }
    }


//    @Scheduled
//    public void evaluateTimeBasedAutomation() {
//        LocalTime now = LocalTime.now()
//                .truncatedTo(ChronoUnit.MINUTES);
//
//        String today = LocalDate.now()
//                .getDayOfWeek()
//                .name()
//                .substring(0, 3);
//
//
//    }
}
