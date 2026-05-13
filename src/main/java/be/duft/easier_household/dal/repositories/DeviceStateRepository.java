package be.duft.easier_household.dal.repositories;

import be.duft.easier_household.dl.entities.DeviceState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceStateRepository extends BaseRepository<DeviceState, Long> {

    public Page<DeviceState> findByDeviceIdOrderByChangedAtDesc(Long deviceId, Pageable pageable);

}
