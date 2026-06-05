package com.bhagwat.scm.carrierService.repository;
import com.bhagwat.scm.carrierService.entity.ReadyToShipOrder;
import com.bhagwat.scm.carrierService.enums.RtsStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface ReadyToShipOrderRepository extends JpaRepository<ReadyToShipOrder, String> {
    List<ReadyToShipOrder> findByCarrierId(String carrierId);
    List<ReadyToShipOrder> findByTrId(String trId);
    List<ReadyToShipOrder> findByStatus(RtsStatus status);
    Optional<ReadyToShipOrder> findByRtsNumber(String rtsNumber);
}
