package com.bhagwat.scm.carrierService.controller;

import com.bhagwat.scm.carrierService.entity.*;
import com.bhagwat.scm.carrierService.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/carrier")
@RequiredArgsConstructor
public class LogisticsOpsController {

    private final TransportExceptionRepository exceptionRepo;
    private final IssueRepository issueRepo;
    private final FreightInvoiceRepository invoiceRepo;
    private final PaymentRepository paymentRepo;
    private final IntegrationConfigRepository integrationRepo;

    // ── Exceptions ──
    @PostMapping("/exceptions")
    public ResponseEntity<TransportException> createException(@RequestBody TransportException e) {
        if (e.getExceptionId() == null) e.setExceptionId("EXC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        if (e.getStatus() == null) e.setStatus("Open");
        return ResponseEntity.ok(exceptionRepo.save(e));
    }
    @GetMapping("/exceptions")
    public ResponseEntity<List<TransportException>> getExceptions(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(status != null ? exceptionRepo.findByStatus(status) : exceptionRepo.findAll());
    }
    @PatchMapping("/exceptions/{id}/status")
    public ResponseEntity<TransportException> updateExceptionStatus(@PathVariable String id, @RequestParam String status) {
        TransportException e = exceptionRepo.findById(id).orElseThrow();
        e.setStatus(status);
        return ResponseEntity.ok(exceptionRepo.save(e));
    }

    // ── Issues ──
    @PostMapping("/issues")
    public ResponseEntity<Issue> createIssue(@RequestBody Issue i) {
        if (i.getIssueId() == null) i.setIssueId("ISS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        if (i.getStatus() == null) i.setStatus("Open");
        return ResponseEntity.ok(issueRepo.save(i));
    }
    @GetMapping("/issues")
    public ResponseEntity<List<Issue>> getIssues(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(status != null ? issueRepo.findByStatus(status) : issueRepo.findAll());
    }
    @PatchMapping("/issues/{id}/status")
    public ResponseEntity<Issue> updateIssueStatus(@PathVariable String id, @RequestParam String status) {
        Issue i = issueRepo.findById(id).orElseThrow();
        i.setStatus(status);
        return ResponseEntity.ok(issueRepo.save(i));
    }

    // ── Invoices ──
    @PostMapping("/invoices")
    public ResponseEntity<FreightInvoice> createInvoice(@RequestBody FreightInvoice inv) {
        if (inv.getInvoiceId() == null) inv.setInvoiceId("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        if (inv.getStatus() == null) inv.setStatus("Pending");
        return ResponseEntity.ok(invoiceRepo.save(inv));
    }
    @GetMapping("/invoices")
    public ResponseEntity<List<FreightInvoice>> getInvoices() { return ResponseEntity.ok(invoiceRepo.findAll()); }
    @PatchMapping("/invoices/{id}/status")
    public ResponseEntity<FreightInvoice> updateInvoiceStatus(@PathVariable String id, @RequestParam String status) {
        FreightInvoice inv = invoiceRepo.findById(id).orElseThrow();
        inv.setStatus(status);
        return ResponseEntity.ok(invoiceRepo.save(inv));
    }

    // ── Payments ──
    @PostMapping("/payments")
    public ResponseEntity<Payment> createPayment(@RequestBody Payment p) {
        if (p.getPaymentId() == null) p.setPaymentId("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return ResponseEntity.ok(paymentRepo.save(p));
    }
    @GetMapping("/payments")
    public ResponseEntity<List<Payment>> getPayments() { return ResponseEntity.ok(paymentRepo.findAll()); }

    // ── Integrations ──
    @PostMapping("/integrations")
    public ResponseEntity<IntegrationConfig> createIntegration(@RequestBody IntegrationConfig ic) {
        if (ic.getIntegrationId() == null) ic.setIntegrationId("INT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return ResponseEntity.ok(integrationRepo.save(ic));
    }
    @GetMapping("/integrations")
    public ResponseEntity<List<IntegrationConfig>> getIntegrations() { return ResponseEntity.ok(integrationRepo.findAll()); }
    @PutMapping("/integrations/{id}")
    public ResponseEntity<IntegrationConfig> updateIntegration(@PathVariable String id, @RequestBody IntegrationConfig ic) {
        ic.setIntegrationId(id);
        return ResponseEntity.ok(integrationRepo.save(ic));
    }
}
