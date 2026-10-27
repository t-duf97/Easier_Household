package be.duft.easier_household.domain.condition;

/**
 * Ce que le domaine sait de l'état actuel d'un capteur, sans savoir que
 * cette valeur vient d'une colonne PostgreSQL (Device.currentValue).
 */
public record SensorState(Long deviceId, String currentValue) {
}
