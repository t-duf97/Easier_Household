package be.duft.easier_household.dl.repositories;

import be.duft.easier_household.dl.entities.Device;
import be.duft.easier_household.dl.enums.DeviceType;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends BaseRepository<Device, Long> {

        List<Device> findByAssignedRoom_Id(Long roomId);

        int countByAssignedRoom_Id(Long roomId);

        List<Device> findByAssignedRoom_Household_Id(Long householdId);

        List<Device> findByTypeAndAssignedRoom_Household_Id(DeviceType deviceType, Long householdId);

        List<Device> findByType(DeviceType deviceType);

        void deleteByAssignedRoom_Household_Id(Long householdId);
    }

