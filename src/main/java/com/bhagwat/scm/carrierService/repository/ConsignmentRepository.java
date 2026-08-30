package com.bhagwat.scm.carrierService.repository;

import com.bhagwat.scm.carrierService.entity.Consignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsignmentRepository extends JpaRepository<Consignment, String> {
    List<Consignment> findByTransportShipmentId(String transportShipmentId);
    Optional<Consignment> findByLabelCode(String labelCode);
}
