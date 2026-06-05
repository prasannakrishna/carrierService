package com.bhagwat.scm.carrierService.repository;
import com.bhagwat.scm.carrierService.entity.ReadyToShipItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface ReadyToShipItemRepository extends JpaRepository<ReadyToShipItem, String> {
    List<ReadyToShipItem> findByRtsId(String rtsId);
}
