package be.duft.easier_household.dal.repositories;

import be.duft.easier_household.dl.entities.Automation;
import be.duft.easier_household.dl.enums.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
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
                        AND t.triggerType = 'TIME'
                        AND t.timeValue = :timeValue
                        AND (t.daysOfWeek IS NULL OR t.daysOfWeek LIKE %:dayOfWeek%)            
            """
    )
    List<Automation> findEnabledAutomationsWithTimeTrigger(
            @Param("timeValue") LocalTime timeValue,
            @Param("dayOfWeek") String dayOfWeek
    );


    /**
     * Pour le scheduler - Cette query permet de trouver les automations dont le type de trigger
     * est le changement d'état d'un device
     * **/
    @Query("""
            SELECT DISTINCT a FROM Automation a
                JOIN a.triggers t 
                WHERE a.isEnabled = true
                AND t.triggerType = 'DEVICE_STATE_CHANGE'
                AND t.device.id = :deviceId
                AND t.expectedStatus =:status
""")
    List<Automation> findEnabledAutomationsByDeviceTrigger(
            @Param("deviceId") Long deviceId,
            @Param("status") DeviceStatus status
    );



}
