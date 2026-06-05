package com.bhagwat.scm.carrierService.repository;

import com.bhagwat.scm.carrierService.entity.TransportException;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransportExceptionRepository extends JpaRepository<TransportException, String> {
    List<TransportException> findByShipmentId(String shipmentId);
    List<TransportException> findByStatus(String status);
}
