package com.bhagwat.scm.carrierService.repository;

import com.bhagwat.scm.carrierService.entity.IntegrationConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntegrationConfigRepository extends JpaRepository<IntegrationConfig, String> {}
