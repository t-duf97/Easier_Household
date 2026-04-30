package be.duft.easier_household.dal.repositories;

import be.duft.easier_household.dl.entities.DeviceState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceStateRepository extends JpaRepository<DeviceState, Long> {
}
