package be.duft.easier_household.dal.repositories;

import be.duft.easier_household.dl.entities.Device;
import be.duft.easier_household.dl.enums.DeviceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends BaseRepository<Device, Long> {

    List<Device> findByRoomId(Long roomId);

    List<Device> findByHouseholdId(Long householdId);

    List<Device> findByDeviceType(DeviceType type);
}
