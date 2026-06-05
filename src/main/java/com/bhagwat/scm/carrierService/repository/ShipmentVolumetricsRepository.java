package com.bhagwat.scm.carrierService.repository;

import com.bhagwat.scm.carrierService.entity.ShipmentVolumetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ShipmentVolumetricsRepository extends JpaRepository<ShipmentVolumetrics, Long> {
    Optional<ShipmentVolumetrics> findByRtsId(String rtsId);
    Optional<ShipmentVolumetrics> findByShipmentId(String shipmentId);
}
