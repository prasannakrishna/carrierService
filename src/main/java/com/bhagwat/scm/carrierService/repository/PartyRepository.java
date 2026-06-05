package com.bhagwat.scm.carrierService.repository;

import com.bhagwat.scm.carrierService.entity.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PartyRepository extends JpaRepository<Party, String> {
    List<Party> findByPartyRole(String partyRole);
}
