package com.bhagwat.scm.carrierService.repository;
import com.bhagwat.scm.carrierService.entity.TransportRequest;
import com.bhagwat.scm.carrierService.enums.TransportRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface TransportRequestRepository extends JpaRepository<TransportRequest, String> {
    List<TransportRequest> findByCarrierId(String carrierId);
    List<TransportRequest> findByStatus(TransportRequestStatus status);
    Optional<TransportRequest> findByTrNumber(String trNumber);
    List<TransportRequest> findByRequestedByPartyId(String partyId);
    List<TransportRequest> findByShippingOrderId(String shippingOrderId);
}
