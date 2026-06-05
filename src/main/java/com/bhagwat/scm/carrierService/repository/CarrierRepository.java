package com.bhagwat.scm.carrierService.repository;
import com.bhagwat.scm.carrierService.entity.Carrier;
import com.bhagwat.scm.carrierService.enums.CarrierType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface CarrierRepository extends JpaRepository<Carrier, String> {
    List<Carrier> findByActiveTrue();
    List<Carrier> findByCarrierType(CarrierType type);
    List<Carrier> findByActiveTrueAndCarrierType(CarrierType type);
}
