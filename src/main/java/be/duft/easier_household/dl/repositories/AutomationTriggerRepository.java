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

    /**
     * Triggers TIME définis par une expression cron, dans des automatisations actives.
     */
    @Query("""
            SELECT t FROM AutomationTrigger t
                JOIN FETCH t.automation a
                WHERE a.isEnabled = true
                AND t.type = 'TIME'
                AND t.cronExpression IS NOT NULL
            """)
    List<AutomationTrigger> findEnabledCronTriggers();

    /**
     * Triggers SUNRISE et SUNSET d'automatisations actives, avec leur foyer (position et fuseau horaire).
     */
    @Query("""
            SELECT t FROM AutomationTrigger t
                JOIN FETCH t.automation a
                JOIN FETCH a.household
                WHERE a.isEnabled = true
                AND (t.type = 'SUNRISE' OR t.type = 'SUNSET')
            """)
    List<AutomationTrigger> findEnabledSunTriggers();
}
