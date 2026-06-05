package com.bhagwat.scm.carrierService.repository;

import com.bhagwat.scm.carrierService.entity.DomainRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DomainRoleRepository extends JpaRepository<DomainRole, String> {
    List<DomainRole> findByRoleType(String roleType);
    List<DomainRole> findByIsActiveTrue();
}
