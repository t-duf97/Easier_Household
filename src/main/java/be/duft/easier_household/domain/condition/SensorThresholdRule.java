package be.duft.easier_household.domain.condition;

import be.duft.easier_household.dl.enums.ThresholdOperator;

import java.util.Optional;

/**
 * LA règle métier : une condition de type "valeur de capteur" est remplie
 * quand la valeur courante du capteur, lue comme un nombre, respecte
 * l'opérateur de seuil défini (>, >=, <, <=, =). Si le capteur est inconnu
 * ou que sa valeur n'est pas un nombre, la condition n'est jamais remplie.
 * <p>
 * Java pur : ni Spring, ni JPA. Testable avec de simples valeurs.
 */
public class SensorThresholdRule {

    private final SensorStatePort statePort;

    public SensorThresholdRule(SensorStatePort statePort) {
        this.statePort = statePort;
    }

    public boolean isMet(Long deviceId, Double threshold, ThresholdOperator operator) {
        if (deviceId == null || threshold == null || operator == null) {
            return false;
        }

        Optional<SensorState> state = statePort.stateOf(deviceId);
        if (state.isEmpty() || state.get().currentValue() == null) {
            return false;
        }

        double currentVal;
        try {
            currentVal = Double.parseDouble(state.get().currentValue());
        } catch (NumberFormatException e) {
            return false;
        }

        return switch (operator) {
            case GT -> currentVal > threshold;
            case GTE -> currentVal >= threshold;
            case LT -> currentVal < threshold;
            case LTE -> currentVal <= threshold;
            case EQ -> currentVal == threshold;
        };
    }
}
