package com.bhagwat.scm.carrierService.repository;

import com.bhagwat.scm.carrierService.entity.LastMileCarrier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LastMileCarrierRepository extends JpaRepository<LastMileCarrier, Long> {
}

