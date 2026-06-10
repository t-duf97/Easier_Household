package be.duft.easier_household.dal.repositories;

import be.duft.easier_household.dl.entities.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface RoomRepository extends BaseRepository<Room, Long> {

    Room findByHouseholdId(Long householdId);

    List<Room> findAllByHouseholdId(Long householdId);
}
