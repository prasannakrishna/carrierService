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
public class LogisticsConfigController {

    private final LogisticsContractRepository contractRepo;
    private final RateCardRepository rateCardRepo;
    private final VehicleConfigRepository vehicleConfigRepo;
    private final NotificationConfigRepository notifRepo;
    private final UserGroupRepository userGroupRepo;

    // ── Contracts ──
    @PostMapping("/contracts")
    public ResponseEntity<LogisticsContract> createContract(@RequestBody LogisticsContract c) {
        if (c.getContractId() == null) c.setContractId("CNT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return ResponseEntity.ok(contractRepo.save(c));
    }
    @GetMapping("/contracts")
    public ResponseEntity<List<LogisticsContract>> getContracts() { return ResponseEntity.ok(contractRepo.findAll()); }
    @PutMapping("/contracts/{id}")
    public ResponseEntity<LogisticsContract> updateContract(@PathVariable String id, @RequestBody LogisticsContract c) { c.setContractId(id); return ResponseEntity.ok(contractRepo.save(c)); }
    @DeleteMapping("/contracts/{id}")
    public ResponseEntity<Void> deleteContract(@PathVariable String id) { contractRepo.deleteById(id); return ResponseEntity.noContent().build(); }

    // ── Rate Cards ──
    @PostMapping("/rate-cards")
    public ResponseEntity<RateCard> createRateCard(@RequestBody RateCard rc) {
        if (rc.getRateCardId() == null) rc.setRateCardId("RC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return ResponseEntity.ok(rateCardRepo.save(rc));
    }
    @GetMapping("/rate-cards")
    public ResponseEntity<List<RateCard>> getRateCards() { return ResponseEntity.ok(rateCardRepo.findAll()); }
    @PutMapping("/rate-cards/{id}")
    public ResponseEntity<RateCard> updateRateCard(@PathVariable String id, @RequestBody RateCard rc) { rc.setRateCardId(id); return ResponseEntity.ok(rateCardRepo.save(rc)); }
    @DeleteMapping("/rate-cards/{id}")
    public ResponseEntity<Void> deleteRateCard(@PathVariable String id) { rateCardRepo.deleteById(id); return ResponseEntity.noContent().build(); }

    // ── Vehicle Config ──
    @PostMapping("/vehicle-config")
    public ResponseEntity<VehicleConfig> createVehicleConfig(@RequestBody VehicleConfig vc) {
        if (vc.getConfigId() == null) vc.setConfigId("VC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return ResponseEntity.ok(vehicleConfigRepo.save(vc));
    }
    @GetMapping("/vehicle-config")
    public ResponseEntity<List<VehicleConfig>> getVehicleConfigs() { return ResponseEntity.ok(vehicleConfigRepo.findAll()); }
    @PutMapping("/vehicle-config/{id}")
    public ResponseEntity<VehicleConfig> updateVehicleConfig(@PathVariable String id, @RequestBody VehicleConfig vc) { vc.setConfigId(id); return ResponseEntity.ok(vehicleConfigRepo.save(vc)); }
    @DeleteMapping("/vehicle-config/{id}")
    public ResponseEntity<Void> deleteVehicleConfig(@PathVariable String id) { vehicleConfigRepo.deleteById(id); return ResponseEntity.noContent().build(); }

    // ── Notification Config ──
    @PostMapping("/notification-config")
    public ResponseEntity<NotificationConfig> createNotifConfig(@RequestBody NotificationConfig nc) {
        if (nc.getConfigId() == null) nc.setConfigId("NC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return ResponseEntity.ok(notifRepo.save(nc));
    }
    @GetMapping("/notification-config")
    public ResponseEntity<List<NotificationConfig>> getNotifConfigs() { return ResponseEntity.ok(notifRepo.findAll()); }
    @PutMapping("/notification-config/{id}")
    public ResponseEntity<NotificationConfig> updateNotifConfig(@PathVariable String id, @RequestBody NotificationConfig nc) { nc.setConfigId(id); return ResponseEntity.ok(notifRepo.save(nc)); }
    @DeleteMapping("/notification-config/{id}")
    public ResponseEntity<Void> deleteNotifConfig(@PathVariable String id) { notifRepo.deleteById(id); return ResponseEntity.noContent().build(); }

    // ── User Groups ──
    @PostMapping("/user-groups")
    public ResponseEntity<UserGroup> createUserGroup(@RequestBody UserGroup ug) {
        if (ug.getGroupId() == null) ug.setGroupId("UG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return ResponseEntity.ok(userGroupRepo.save(ug));
    }
    @GetMapping("/user-groups")
    public ResponseEntity<List<UserGroup>> getUserGroups() { return ResponseEntity.ok(userGroupRepo.findAll()); }
    @PutMapping("/user-groups/{id}")
    public ResponseEntity<UserGroup> updateUserGroup(@PathVariable String id, @RequestBody UserGroup ug) { ug.setGroupId(id); return ResponseEntity.ok(userGroupRepo.save(ug)); }
    @DeleteMapping("/user-groups/{id}")
    public ResponseEntity<Void> deleteUserGroup(@PathVariable String id) { userGroupRepo.deleteById(id); return ResponseEntity.noContent().build(); }
}
