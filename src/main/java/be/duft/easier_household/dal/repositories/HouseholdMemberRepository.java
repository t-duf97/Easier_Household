package be.duft.easier_household.dal.repositories;

import be.duft.easier_household.dl.entities.Household;
import be.duft.easier_household.dl.entities.HouseholdMember;
import be.duft.easier_household.dl.embeddable.HouseholdMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface HouseholdMemberRepository extends BaseRepository<HouseholdMember, HouseholdMemberId> {

    public Set<HouseholdMember> findByHousehold(Household household);

}
