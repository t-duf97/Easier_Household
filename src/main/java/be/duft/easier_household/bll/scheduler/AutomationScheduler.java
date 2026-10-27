package be.duft.easier_household.bll.scheduler;

import be.duft.easier_household.dl.repositories.AutomationRepository;
import be.duft.easier_household.dl.repositories.DeviceRepository;
import be.duft.easier_household.dl.repositories.DeviceStateRepository;
import be.duft.easier_household.dl.entities.*;
import be.duft.easier_household.dl.enums.ChangeSource;
import be.duft.easier_household.dl.enums.DeviceStatus;
import be.duft.easier_household.domain.condition.SensorThresholdRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutomationScheduler {

    private final AutomationRepository automationRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceStateRepository deviceStateRepository;
    private final SensorThresholdRule sensorThresholdRule;


    public void evaluateDeviceStateAutomations(Long deviceId, DeviceStatus newStatus) {
        List<Automation> automations = automationRepository
                .findEnabledAutomationsByDeviceTrigger(deviceId, newStatus);

        log.info("Device '{}' changed to {} - found {} reactive automation(s)",
                deviceId, newStatus,  automations.size());

        for (Automation automation : automations) {
            try {
                evaluateAndExecute(automation);
            } catch (Exception e) {
                log.error("Error in reactive automation '{}' : {}", automation.getName(), e.getMessage(), e);
            }
        }
    }

    @Scheduled(cron = "0 * * * * *") // A la seconde 0 de chaque minute
    public void evaluateTimeBasedAutomations() {
        LocalTime now = LocalTime.now();
        String today = LocalDate.now().getDayOfWeek().name().substring(0, 3);

        log.debug("Evaluating time based automations at {}", now);

        List<Automation> automations = automationRepository
                .findEnabledAutomationsWithTimeTrigger(now, today);

        log.info("Found {} TIME automations to evaluate at {}", automations.size(), now);

        for (Automation automation : automations) {
            try {
                evaluateAndExecute(automation);
            } catch (Exception e) {
                log.error("Error  evaluating automation {} (id = {}): {}",
                        automation.getName(), automation.getId(), e.getMessage(), e);
            }
        }

    }

    public void evaluateAndExecute(Automation automation) {
        log.info("Evaluation Automation: '{}' (id={})", automation.getName(), automation.getId());

        boolean conditionsMet = checkConditions(automation);

        if (!conditionsMet) {
            log.debug("Conditions not met for automation: '{}' - skipping automation",
                    automation.getName());
            return;
        }

        executeActions(automation);

        automation.setLastTriggeredAt(LocalDateTime.now());
        automationRepository.save(automation);

        log.info("Automation: '{}' executed successfully at {}",
                automation.getName(), automation.getLastTriggeredAt());
    }

    private boolean checkConditions(Automation automation) {
        List<AutomationCondition> conditions = automation.getConditions();

        if (conditions == null || conditions.isEmpty()) {
            return true;
        }
        return switch (automation.getLogic()) {
            case ALL -> conditions.stream().allMatch(this::evaluateCondition);
            case ANY -> conditions.stream().anyMatch(this::evaluateCondition);
        };
    }

    private boolean evaluateCondition(AutomationCondition condition) {
        return switch (condition.getType()) {
            case DEVICE_STATUS -> {
                if (condition.getDevice() == null) yield false;
                Device device = deviceRepository.findById(condition.getDevice().getId())
                        .orElse(null);
                yield device != null && device.getStatus() == condition.getStatus();
            }

            case SENSOR_VALUE -> {
                Long deviceId = condition.getDevice() == null ? null : condition.getDevice().getId();
                yield sensorThresholdRule.isMet(
                        deviceId, condition.getThresholdValue(), condition.getThresholdOperator());
            }

            case TIME_RANGE -> {
                LocalTime now = LocalTime.now();
                if (condition.getTimeFrom() == null || condition.getTimeTo() == null) yield false;
                if (condition.getTimeFrom().isBefore(condition.getTimeTo())) {
                    yield now.isAfter(condition.getTimeFrom()) && now.isBefore(condition.getTimeTo());
                } else {
                    yield now.isAfter(condition.getTimeFrom()) || now.isBefore(condition.getTimeTo());
                }
            }

            case DAY_OF_WEEK -> {
                if (condition.getDaysOfWeek() == null) yield false;
                String today = LocalDate.now().getDayOfWeek().name().substring(0, 3);
                yield condition.getDaysOfWeek().contains(today);
            }

            case NO_MOTION_SINCE -> {
                if (condition.getDevice() == null || condition.getDurationInMinutes() == null) yield false;
                LocalDateTime threshold = LocalDateTime.now().minusMinutes(condition.getDurationInMinutes());
                yield !deviceStateRepository.existsByDeviceIdAndChangedAtAfter(condition.getDevice().getId(), threshold);
            }

        };
    }

    // Execute les actions dans l'ordre défini par execution Order!
    private void executeActions(Automation automation) {
        List<AutomationAction> actions = automation.getActions().stream()
                .sorted(Comparator.comparingInt(AutomationAction::getExecutionOrder))
                .collect(Collectors.toList());

        for (AutomationAction action : actions) {
            executeAction(action, automation);
        }
    }

    // Execute une action selon son type.
    public void executeAction(AutomationAction action, Automation automation) {
        switch (action.getType()) {
            case SET_DEVICE_STATUS -> {
                if (action.getDevice() == null) return;
                Device device = deviceRepository.findById(action.getDevice().getId()).orElse(null);
                if (device == null) return;

                device.setStatus(action.getStatus());
                device.setUpdatedAt(LocalDateTime.now());
                deviceRepository.save(device);

                DeviceState deviceState = new DeviceState();
                deviceState.setDevice(device);
                deviceState.setValue(action.getTargetValue());
                deviceState.setChangeSource(ChangeSource.AUTOMATION);
                deviceState.setChangedAt(LocalDateTime.now());
                deviceStateRepository.save(deviceState);

                log.info("Action executed : Device '{}' status set to {} by automation '{}'",
                        device.getName(), action.getStatus(), automation.getName());
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
                if (action.getDelayInSeconds() > 0) {
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
}



