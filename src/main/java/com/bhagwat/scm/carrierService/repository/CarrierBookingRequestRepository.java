package com.bhagwat.scm.carrierService.repository;
import com.bhagwat.scm.carrierService.entity.CarrierBookingRequest;
import com.bhagwat.scm.carrierService.enums.CbrStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface CarrierBookingRequestRepository extends JpaRepository<CarrierBookingRequest, String> {
    List<CarrierBookingRequest> findByRequestedByPartyId(String partyId);
    List<CarrierBookingRequest> findByStatus(CbrStatus status);
    Optional<CarrierBookingRequest> findByCbrNumber(String cbrNumber);
    List<CarrierBookingRequest> findByRequestedByPartyIdAndStatus(String partyId, CbrStatus status);
}
