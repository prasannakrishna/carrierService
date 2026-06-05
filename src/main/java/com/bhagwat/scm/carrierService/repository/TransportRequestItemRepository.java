package com.bhagwat.scm.carrierService.repository;
import com.bhagwat.scm.carrierService.entity.TransportRequestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface TransportRequestItemRepository extends JpaRepository<TransportRequestItem, String> {
    List<TransportRequestItem> findByTrId(String trId);
    void deleteByTrId(String trId);
}
