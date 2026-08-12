package com.bhagwat.scm.carrierService.repository;

import com.bhagwat.scm.carrierService.entity.FacilityOperator;
import com.bhagwat.scm.carrierService.entity.FacilityOperator.OperatorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FacilityOperatorRepository extends JpaRepository<FacilityOperator, UUID> {

    Optional<FacilityOperator> findByIdAndOwningService(UUID id, String owningService);

    boolean existsByOwningServiceAndFacilityOwnerOrgIdAndOperatorName(
            String owningService, UUID facilityOwnerOrgId, String operatorName);

    Optional<FacilityOperator> findByPhoneNumberAndOwningService(String phoneNumber, String owningService);

    Optional<FacilityOperator> findByPhoneNumberAndFacilityOwnerOrgIdAndStatusIn(
            String phoneNumber, UUID facilityOwnerOrgId, java.util.List<OperatorStatus> statuses);
}
