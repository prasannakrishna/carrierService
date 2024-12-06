package com.bhagwat.scm.carrierService.repository;

import com.bhagwat.scm.carrierService.entity.LastMileCarrierVehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LastMileCarrierVehicleRepository extends JpaRepository<LastMileCarrierVehicle, Long> {
}

