package com.bhagwat.scm.carrierService.repository;

import com.bhagwat.scm.carrierService.entity.FreightInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FreightInvoiceRepository extends JpaRepository<FreightInvoice, String> {
    List<FreightInvoice> findByStatus(String status);
}
