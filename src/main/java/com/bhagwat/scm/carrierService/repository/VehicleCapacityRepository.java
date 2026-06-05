package com.bhagwat.scm.carrierService.repository;

import com.bhagwat.scm.carrierService.entity.VehicleCapacity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VehicleCapacityRepository extends JpaRepository<VehicleCapacity, String> {
    List<VehicleCapacity> findByStatus(String status);
    VehicleCapacity findByVehicleType(String vehicleType);
}
