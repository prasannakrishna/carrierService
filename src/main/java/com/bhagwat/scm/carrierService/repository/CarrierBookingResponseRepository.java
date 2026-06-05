package com.bhagwat.scm.carrierService.repository;
import com.bhagwat.scm.carrierService.entity.CarrierBookingResponse;
import com.bhagwat.scm.carrierService.enums.CbrRespStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface CarrierBookingResponseRepository extends JpaRepository<CarrierBookingResponse, String> {
    List<CarrierBookingResponse> findByCbrId(String cbrId);
    List<CarrierBookingResponse> findByCbrIdAndStatus(String cbrId, CbrRespStatus status);
    Optional<CarrierBookingResponse> findByCbrRespNumber(String cbrRespNumber);
}
