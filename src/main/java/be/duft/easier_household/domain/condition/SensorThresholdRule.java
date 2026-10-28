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
        return state.isPresent() && satisfies(state.get().currentValue(), threshold, operator);
    }

    /**
     * Vrai si {@code value}, lue comme un nombre, respecte l'opérateur de seuil.
     * Une valeur absente ou non numérique ne respecte jamais le seuil.
     */
    public static boolean satisfies(String value, double threshold, ThresholdOperator operator) {
        if (value == null || operator == null) {
            return false;
        }
        double numeric;
        try {
            numeric = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return false;
        }
        return switch (operator) {
            case GT -> numeric > threshold;
            case GTE -> numeric >= threshold;
            case LT -> numeric < threshold;
            case LTE -> numeric <= threshold;
            case EQ -> numeric == threshold;
        };
    }

    /**
     * Vrai au moment où le seuil est franchi : la nouvelle valeur le respecte et l'ancienne non
     * (ou était absente). Rester au-delà du seuil ne déclenche pas une seconde fois.
     */
    public static boolean crosses(String previousValue, String newValue, double threshold, ThresholdOperator operator) {
        return satisfies(newValue, threshold, operator) && !satisfies(previousValue, threshold, operator);
    }
}
