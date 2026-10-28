package be.duft.easier_household.dl.repositories;

import be.duft.easier_household.dl.entities.AutomationTrigger;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutomationTriggerRepository extends BaseRepository<AutomationTrigger, Long> {

    /**
     * Triggers SENSOR_THRESHOLD qui surveillent ce device, dans des automatisations actives.
     */
    @Query("""
            SELECT t FROM AutomationTrigger t
                JOIN FETCH t.automation a
                WHERE a.isEnabled = true
                AND t.type = 'SENSOR_THRESHOLD'
                AND t.device.id = :deviceId
            """)
    List<AutomationTrigger> findEnabledSensorThresholdTriggers(@Param("deviceId") Long deviceId);
}
