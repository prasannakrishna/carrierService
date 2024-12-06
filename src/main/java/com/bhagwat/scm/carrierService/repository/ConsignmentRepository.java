package com.bhagwat.scm.carrierService.repository;

import com.bhagwat.scm.carrierService.entity.Consignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsignmentRepository extends JpaRepository<Consignment, Long> {
}
