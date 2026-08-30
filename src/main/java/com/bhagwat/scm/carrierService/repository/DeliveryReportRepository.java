package com.bhagwat.scm.carrierService.repository;

import com.bhagwat.scm.carrierService.entity.DeliveryReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryReportRepository extends JpaRepository<DeliveryReport, String> {
    Optional<DeliveryReport> findByConsignmentId(String consignmentId);
    Optional<DeliveryReport> findByIdempotencyKey(String idempotencyKey);
}
