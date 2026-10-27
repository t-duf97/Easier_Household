package be.duft.easier_household.domain.condition;

import be.duft.easier_household.dl.enums.ThresholdOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Aucune annotation Spring, aucune base de données : le fake ci-dessous EST
 * l'implémentation du port, tenue en mémoire par une simple lambda.
 */
class SensorThresholdRuleTest {

    @Test
    @DisplayName("25.5 est bien > 24.0 -> condition remplie")
    void meetsThreshold() {
        SensorStatePort inMemoryPort = deviceId -> Optional.of(new SensorState(deviceId, "25.5"));
        SensorThresholdRule rule = new SensorThresholdRule(inMemoryPort);

        assertTrue(rule.isMet(1L, 24.0, ThresholdOperator.GT));
    }

    @Test
    @DisplayName("18.0 n'est pas > 24.0 -> condition non remplie")
    void doesNotMeetThreshold() {
        SensorStatePort inMemoryPort = deviceId -> Optional.of(new SensorState(deviceId, "18.0"));
        SensorThresholdRule rule = new SensorThresholdRule(inMemoryPort);

        assertFalse(rule.isMet(1L, 24.0, ThresholdOperator.GT));
    }

    @Test
    @DisplayName("Valeur de capteur non numérique -> condition non remplie, pas d'exception")
    void nonNumericValueIsSafelyFalse() {
        SensorStatePort inMemoryPort = deviceId -> Optional.of(new SensorState(deviceId, "hors service"));
        SensorThresholdRule rule = new SensorThresholdRule(inMemoryPort);

        assertFalse(rule.isMet(1L, 24.0, ThresholdOperator.GT));
    }

    @Test
    @DisplayName("Capteur inconnu -> condition non remplie")
    void unknownDeviceIsSafelyFalse() {
        SensorStatePort inMemoryPort = deviceId -> Optional.empty();
        SensorThresholdRule rule = new SensorThresholdRule(inMemoryPort);

        assertFalse(rule.isMet(1L, 24.0, ThresholdOperator.GT));
    }
}
