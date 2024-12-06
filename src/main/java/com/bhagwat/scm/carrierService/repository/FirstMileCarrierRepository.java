package com.bhagwat.scm.carrierService.repository;

import com.bhagwat.scm.carrierService.entity.FirstMileCarrier;
import com.bhagwat.scm.carrierService.entity.LastMileCarrier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FirstMileCarrierRepository extends JpaRepository<FirstMileCarrier, Long> {
}
