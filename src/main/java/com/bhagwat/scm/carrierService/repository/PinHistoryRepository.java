package com.bhagwat.scm.carrierService.repository;

import com.bhagwat.scm.carrierService.entity.PinHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PinHistoryRepository extends JpaRepository<PinHistory, UUID> {

    /**
     * Returns last N PIN history entries for an operator, ordered most-recent first.
     */
    List<PinHistory> findTop3ByOperatorIdOrderByChangedAtDesc(UUID operatorId);
}
