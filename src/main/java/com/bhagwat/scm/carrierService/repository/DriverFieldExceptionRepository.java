package com.bhagwat.scm.carrierService.repository;

import com.bhagwat.scm.carrierService.entity.DriverFieldException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DriverFieldExceptionRepository extends JpaRepository<DriverFieldException, UUID> {
}
