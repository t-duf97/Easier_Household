package be.duft.easier_household.domain.condition;

import java.util.Optional;

/**
 * Port : ce dont le domaine a besoin pour connaître l'état d'un capteur.
 * Vocabulaire métier, aucune trace de JPA/Repository ici.
 * Aujourd'hui implémenté par JpaSensorStateAdapter.
 */
public interface SensorStatePort {

    Optional<SensorState> stateOf(Long deviceId);
}
