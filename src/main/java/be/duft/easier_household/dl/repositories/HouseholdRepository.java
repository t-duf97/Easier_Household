package be.duft.easier_household.dl.repositories;

import be.duft.easier_household.dl.entities.Household;
import org.springframework.stereotype.Repository;

@Repository
public interface HouseholdRepository extends BaseRepository<Household, Long> {

}
