package be.duft.easier_household.bll.events;

import be.duft.easier_household.dl.enums.DeviceStatus;

/**
 * Publié quand le statut d'un device change réellement (ancien statut différent du nouveau).
 */
public record DeviceStatusChangedEvent(Long deviceId, DeviceStatus newStatus) {
}
