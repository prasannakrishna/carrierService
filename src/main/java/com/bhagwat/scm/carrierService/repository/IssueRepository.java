package com.bhagwat.scm.carrierService.repository;

import com.bhagwat.scm.carrierService.entity.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, String> {
    List<Issue> findByShipmentId(String shipmentId);
    List<Issue> findByStatus(String status);
}
