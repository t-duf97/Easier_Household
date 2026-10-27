package be.duft.easier_household.dl.repositories;

import be.duft.easier_household.dl.entities.DeviceState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface DeviceStateRepository extends BaseRepository<DeviceState, Long> {

    public Page<DeviceState> findByDeviceIdOrderByChangedAtDesc(Long deviceId, Pageable pageable);

    public boolean existsByDeviceIdAndChangedAtAfter (Long deviceId, LocalDateTime changedAt);

    public void deleteByDeviceId(Long deviceId);
}
