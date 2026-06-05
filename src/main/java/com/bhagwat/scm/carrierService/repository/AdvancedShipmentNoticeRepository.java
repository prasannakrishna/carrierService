package com.bhagwat.scm.carrierService.repository;
import com.bhagwat.scm.carrierService.entity.AdvancedShipmentNotice;
import com.bhagwat.scm.carrierService.enums.AsnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface AdvancedShipmentNoticeRepository extends JpaRepository<AdvancedShipmentNotice, String> {
    List<AdvancedShipmentNotice> findByRtsId(String rtsId);
    List<AdvancedShipmentNotice> findBySentToPartyId(String partyId);
    List<AdvancedShipmentNotice> findByStatus(AsnStatus status);
    List<AdvancedShipmentNotice> findBySentToPartyIdAndStatus(String partyId, AsnStatus status);
}
