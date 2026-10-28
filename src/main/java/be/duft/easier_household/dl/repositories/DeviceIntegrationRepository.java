package be.duft.easier_household.dl.repositories;

import be.duft.easier_household.dl.entities.DeviceIntegration;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface DeviceIntegrationRepository extends BaseRepository<DeviceIntegration, Long> {

    void deleteByDevice_AssignedRoom_Household_Id(Long householdId);

    void deleteByDeviceIdIn(Collection<Long> deviceIds);
}
