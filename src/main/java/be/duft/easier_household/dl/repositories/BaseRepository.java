package be.duft.easier_household.dl.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;


@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {
}
















//@NoRepositoryBean Super important car sinon bug au démarrage
// Car création d'une Bean pour une entité qui n'existe pas?