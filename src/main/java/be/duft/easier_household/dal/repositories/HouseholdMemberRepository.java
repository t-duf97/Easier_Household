package be.duft.easier_household.dal.repositories;

import be.duft.easier_household.dl.entities.HouseholdMember;
import be.duft.easier_household.dl.embeddable.HouseholdMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HouseholdMemberRepository extends BaseRepository<HouseholdMember, HouseholdMemberId> {
}
