package com.bhagwat.scm.carrierService.repository;
import com.bhagwat.scm.carrierService.entity.ShipmentMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface ShipmentMilestoneRepository extends JpaRepository<ShipmentMilestone, String> {
    List<ShipmentMilestone> findByTsId(String tsId);
    List<ShipmentMilestone> findByTsIdOrderByMilestoneDateTimeAsc(String tsId);
    Optional<ShipmentMilestone> findByIdempotencyKey(String idempotencyKey);
}
