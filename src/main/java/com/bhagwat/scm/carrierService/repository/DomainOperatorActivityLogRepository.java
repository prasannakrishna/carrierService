package com.bhagwat.scm.carrierService.repository;

import com.bhagwat.scm.carrierService.entity.DomainOperatorActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface DomainOperatorActivityLogRepository extends JpaRepository<DomainOperatorActivityLog, UUID> {

    @Query("""
        SELECT l FROM DomainOperatorActivityLog l
        WHERE l.operatorId = :operatorId
          AND (:facilityId IS NULL OR l.facilityId = :facilityId)
          AND (:actionType IS NULL OR l.actionType = :actionType)
          AND (:entityType IS NULL OR l.entityType = :entityType)
          AND l.performedAt >= :from
          AND l.performedAt <= :to
        ORDER BY l.performedAt DESC
        """)
    Page<DomainOperatorActivityLog> findByFilters(
            @Param("operatorId") UUID operatorId,
            @Param("facilityId") UUID facilityId,
            @Param("actionType") String actionType,
            @Param("entityType") String entityType,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable);
}
