package be.duft.easier_household.dal.repositories;

import be.duft.easier_household.dl.entities.Automation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AutomationRepository extends BaseRepository<Automation, Long> {
}
