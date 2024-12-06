package com.bhagwat.scm.carrierService.repository;

import com.bhagwat.scm.carrierService.entity.VehicleConsignmentLinking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleConsignmentLinkingRepository extends JpaRepository<VehicleConsignmentLinking, Long> {
}

