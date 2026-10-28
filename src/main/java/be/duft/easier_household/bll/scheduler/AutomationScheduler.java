package be.duft.easier_household.bll.scheduler;

import be.duft.easier_household.bll.events.DeviceStatusChangedEvent;
import be.duft.easier_household.dl.repositories.AutomationRepository;
import be.duft.easier_household.dl.repositories.DeviceRepository;
import be.duft.easier_household.dl.repositories.DeviceStateRepository;
import be.duft.easier_household.dl.entities.*;
import be.duft.easier_household.dl.enums.ChangeSource;
import be.duft.easier_household.domain.condition.SensorThresholdRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AutomationScheduler {

    private final AutomationRepository automationRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceStateRepository deviceStateRepository;
    private final SensorThresholdRule sensorThresholdRule;
    private final TransactionTemplate newTransaction;

    public AutomationScheduler(AutomationRepository automationRepository,
                               DeviceRepository deviceRepository,
                               DeviceStateRepository deviceStateRepository,
                               SensorThresholdRule sensorThresholdRule,
                               PlatformTransactionManager transactionManager) {
        this.automationRepository = automationRepository;
        this.deviceRepository = deviceRepository;
        this.deviceStateRepository = deviceStateRepository;
        this.sensorThresholdRule = sensorThresholdRule;
        this.newTransaction = new TransactionTemplate(transactionManager);
        this.newTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /**
     * Déclenche les automatisations DEVICE_STATE_CHANGE une fois le changement d'état commité :
     * une automatisation en échec ne peut plus annuler le changement qui l'a déclenchée.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDeviceStatusChanged(DeviceStatusChangedEvent event) {
        List<Long> automationIds = newTransaction.execute(status -> automationRepository
                .findEnabledAutomationsByDeviceTrigger(event.deviceId(), event.newStatus())
                .stream().map(Automation::getId).toList());

        log.info("Device '{}' changed to {} - found {} reactive automation(s)",
                event.deviceId(), event.newStatus(), automationIds.size());

        runEachInOwnTransaction(automationIds);
    }

    /**
     * Chaque automatisation tourne dans sa propre transaction : l'échec de l'une n'affecte pas les autres.
     */
    private void runEachInOwnTransaction(List<Long> automationIds) {
        for (Long id : automationIds) {
            try {
                newTransaction.executeWithoutResult(status ->
                        automationRepository.findById(id).ifPresent(this::evaluateAndExecute));
            } catch (Exception e) {
                log.error("Error in automation id={} : {}", id, e.getMessage(), e);
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

                if (action.getStatus() == null) {
                    log.warn("Automation '{}': SET_DEVICE_STATUS action {} has no target status",
                            automation.getName(), action.getId());
                    return;
                }

                device.setStatus(action.getStatus());
                device.setUpdatedAt(LocalDateTime.now());
                deviceRepository.save(device);
                recordState(device);

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
                recordState(device);

                log.info("Action executed : Device '{}' value set to {} by automation '{}'",
                        device.getName(), action.getTargetValue(), automation.getName());
            }
            case SEND_NOTIFICATION -> {
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

    /**
     * Historise l'état courant du device (statut + valeur) comme un changement fait par une automatisation.
     */
    private void recordState(Device device) {
        DeviceState state = new DeviceState();
        state.setDevice(device);
        state.setStatus(device.getStatus());
        state.setValue(device.getCurrentValue());
        state.setChangeSource(ChangeSource.AUTOMATION);
        state.setChangedAt(LocalDateTime.now());
        deviceStateRepository.save(state);
    }
}



