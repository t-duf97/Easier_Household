package be.duft.easier_household.dal.repositories;

import be.duft.easier_household.dl.entities.Device;
import be.duft.easier_household.dl.enums.DeviceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends BaseRepository<Device, Long> {

        List<Device> findByAssignedRoom_Id(Long roomId);

        int countByRoomId(Long roomId);

        List<Device> findByAssignedRoom_Household_Id(Long householdId);

        List<Device> findByDeviceTypeAndAssignedRoom_Household_Id(DeviceType deviceType, Long householdId);

        List<Device> findByDeviceType(DeviceType deviceType);
    }

