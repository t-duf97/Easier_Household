package be.duft.easier_household.bll.events;

/**
 * Publié quand la valeur courante d'un device change réellement (relevé de capteur, saisie manuelle).
 */
public record DeviceValueChangedEvent(Long deviceId, String previousValue, String newValue) {
}
