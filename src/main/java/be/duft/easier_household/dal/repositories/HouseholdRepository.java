package be.duft.easier_household.dal.repositories;

import be.duft.easier_household.dl.entities.Household;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HouseholdRepository extends BaseRepository<Household, Long> {

}
