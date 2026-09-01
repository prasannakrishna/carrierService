package com.bhagwat.scm.carrierService.repository;
import com.bhagwat.scm.carrierService.entity.TransportShipment;
import com.bhagwat.scm.carrierService.enums.TransportShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface TransportShipmentRepository extends JpaRepository<TransportShipment, String> {
    List<TransportShipment> findByCarrierId(String carrierId);
    List<TransportShipment> findByStatus(TransportShipmentStatus status);
    Optional<TransportShipment> findByTsNumber(String tsNumber);
    List<TransportShipment> findByTransportPlanId(String planId);
    List<TransportShipment> findByCarrierIdAndStatus(String carrierId, TransportShipmentStatus status);
    List<TransportShipment> findByVehicleId(String vehicleId);
    List<TransportShipment> findByRtsId(String rtsId);
}
