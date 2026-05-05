package be.duft.easier_household.dal.repositories;

import be.duft.easier_household.dl.entities.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends BaseRepository<Device, Long> {

    public List<Device> findByRoomId(Long roomId);
}
