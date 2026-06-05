package com.bhagwat.scm.carrierService.repository;

import com.bhagwat.scm.carrierService.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DriverRepository extends JpaRepository<Driver, String> {
    List<Driver> findByFleetId(String fleetId);
    List<Driver> findByStatus(String status);
}
