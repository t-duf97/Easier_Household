package be.duft.easier_household.config;

import be.duft.easier_household.domain.condition.SensorStatePort;
import be.duft.easier_household.domain.condition.SensorThresholdRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Le seul endroit du projet où le domaine et Spring se rencontrent :
 * SensorThresholdRule (Java pur) reçoit ici son implémentation du port,
 * choisie et injectée par Spring.
 */
@Configuration
public class ConditionConfig {

    @Bean
    public SensorThresholdRule sensorThresholdRule(SensorStatePort sensorStatePort) {
        return new SensorThresholdRule(sensorStatePort);
    }
}
