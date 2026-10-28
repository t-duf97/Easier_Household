package be.duft.easier_household.dl.repositories;

import be.duft.easier_household.dl.entities.Automation;
import be.duft.easier_household.dl.enums.DeviceStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface AutomationRepository extends BaseRepository<Automation, Long> {

    List<Automation> findByHouseholdId(Long householdId);

    List<Automation> findByHouseholdIdAndEnabledTrue(Long householdId);

    List<Automation> findByHouseholdIdAndEnabledFalse(Long householdId);


    /**
     * Cette query permet de trouver les automations dont le type de trigger est de type TIME actives.
     */
    @Query("""
            SELECT DISTINCT a FROM Automation a
                        JOIN a.triggers t
                        WHERE a.isEnabled = true
                        AND t.type = 'TIME'
                        AND t.timeValue BETWEEN :minuteStart AND :minuteEnd
                        AND (t.daysOfWeek IS NULL OR t.daysOfWeek LIKE %:dayOfWeek%)"""
    )
    List<Automation> findEnabledAutomationsWithTimeTrigger(
            @Param("minuteStart") LocalTime minuteStart,
            @Param("minuteEnd") LocalTime minuteEnd,
            @Param("dayOfWeek") String dayOfWeek
    );

    default List<Automation> findEnabledAutomationsWithTimeTrigger(LocalTime minute, String dayOfWeek) {
        return findEnabledAutomationsWithTimeTrigger(minute, minute.plusSeconds(59), dayOfWeek);
    }


    /**
     * Pour le scheduler - Cette query permet de trouver les automations dont le type de trigger
     * est le changement d'état d'un device
     * **/
    @Query("""
            SELECT DISTINCT a FROM Automation a
                JOIN a.triggers t 
                WHERE a.isEnabled = true
                AND t.type = 'DEVICE_STATE_CHANGE'
                AND t.device.id = :deviceId
                AND t.status = :status
""")
    List<Automation> findEnabledAutomationsByDeviceTrigger(
            @Param("deviceId") Long deviceId,
            @Param("status") DeviceStatus status
    );



}
