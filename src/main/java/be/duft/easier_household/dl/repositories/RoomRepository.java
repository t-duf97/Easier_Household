package be.duft.easier_household.dl.repositories;

import be.duft.easier_household.dl.entities.Room;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends BaseRepository<Room, Long> {

    Room findByHouseholdId(Long householdId);

    List<Room> findAllByHouseholdId(Long householdId);

    void deleteByHouseholdId(Long householdId);
}
