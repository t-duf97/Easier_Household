package be.duft.easier_household.dal.repositories;

import be.duft.easier_household.dl.entities.AutomationAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AutomationActionRepository extends BaseRepository<AutomationAction, Long> {
}
