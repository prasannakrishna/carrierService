package com.bhagwat.scm.carrierService.repository;

import com.bhagwat.scm.carrierService.entity.DomainWorker;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DomainWorkerRepository extends JpaRepository<DomainWorker, String> {
    List<DomainWorker> findByRoleId(String roleId);
    List<DomainWorker> findByStatus(String status);
    Optional<DomainWorker> findByPhone(String phone);
    Optional<DomainWorker> findByPhoneAndPin(String phone, String pin);
    Optional<DomainWorker> findByDriverId(String driverId);
    Optional<DomainWorker> findByPlatformUserId(String platformUserId);
}
