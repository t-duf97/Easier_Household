package be.duft.easier_household.dl.repositories;

import be.duft.easier_household.dl.entities.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends BaseRepository<User, Long> {

    public Optional<User> findByEmail(String email);

    public boolean existsByEmail(String email);
}
