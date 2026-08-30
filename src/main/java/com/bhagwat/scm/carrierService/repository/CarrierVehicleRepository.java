package com.bhagwat.scm.carrierService.repository;
import com.bhagwat.scm.carrierService.entity.CarrierVehicle;
import com.bhagwat.scm.carrierService.enums.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface CarrierVehicleRepository extends JpaRepository<CarrierVehicle, String> {
    List<CarrierVehicle> findByCarrierId(String carrierId);
    List<CarrierVehicle> findByCarrierIdAndStatus(String carrierId, VehicleStatus status);
    Optional<CarrierVehicle> findByVehicleNumber(String vehicleNumber);
}
