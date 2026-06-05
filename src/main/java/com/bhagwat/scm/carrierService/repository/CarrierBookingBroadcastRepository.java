package com.bhagwat.scm.carrierService.repository;
import com.bhagwat.scm.carrierService.entity.CarrierBookingBroadcast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface CarrierBookingBroadcastRepository extends JpaRepository<CarrierBookingBroadcast, String> {
    List<CarrierBookingBroadcast> findByCbrId(String cbrId);
    List<CarrierBookingBroadcast> findByCarrierId(String carrierId);
}
