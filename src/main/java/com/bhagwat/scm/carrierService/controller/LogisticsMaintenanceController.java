package com.bhagwat.scm.carrierService.controller;

import com.bhagwat.scm.carrierService.entity.*;
import com.bhagwat.scm.carrierService.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/carrier")
@RequiredArgsConstructor
public class LogisticsMaintenanceController {

    private final DriverRepository driverRepo;
    private final FleetRepository fleetRepo;
    private final OperatingPincodeRepository opPincodeRepo;
    private final BookingPincodeRepository bkPincodeRepo;
    private final PartyRepository partyRepo;
    private final SiteRepository siteRepo;

    private final BCryptPasswordEncoder pinEncoder = new BCryptPasswordEncoder(10);

    // ── Drivers ──
    @PostMapping("/drivers")
    public ResponseEntity<Driver> createDriver(@RequestBody Driver d) {
        if (d.getDriverId() == null) d.setDriverId("DRV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        d.setPinHash(null); // PIN is set separately via /drivers/{id}/pin, never accepted as raw hash here
        return ResponseEntity.ok(driverRepo.save(d));
    }
    @GetMapping("/drivers")
    public ResponseEntity<List<Driver>> getDrivers() { return ResponseEntity.ok(driverRepo.findAll()); }
    @GetMapping("/drivers/{id}")
    public ResponseEntity<Driver> getDriver(@PathVariable String id) { return ResponseEntity.ok(driverRepo.findById(id).orElseThrow()); }
    @PutMapping("/drivers/{id}")
    public ResponseEntity<Driver> updateDriver(@PathVariable String id, @RequestBody Driver d) {
        d.setDriverId(id);
        // Preserve the existing PIN hash — this endpoint updates driver profile fields,
        // not credentials; PIN rotation goes through /drivers/{id}/pin.
        driverRepo.findById(id).ifPresent(existing -> d.setPinHash(existing.getPinHash()));
        return ResponseEntity.ok(driverRepo.save(d));
    }
    @DeleteMapping("/drivers/{id}")
    public ResponseEntity<Void> deleteDriver(@PathVariable String id) { driverRepo.deleteById(id); return ResponseEntity.noContent().build(); }

    /** Sets (or rotates) a driver's login PIN for carrier-driver-app. Hashed server-side — never stored or accepted as plaintext/hash from elsewhere. */
    @PatchMapping("/drivers/{id}/pin")
    public ResponseEntity<Void> setDriverPin(@PathVariable String id, @RequestBody DriverPinRequest request) {
        Driver driver = driverRepo.findById(id).orElseThrow(() -> new java.util.NoSuchElementException("Driver not found: " + id));
        driver.setPinHash(pinEncoder.encode(request.getPin()));
        driverRepo.save(driver);
        return ResponseEntity.noContent().build();
    }

    public record DriverPinRequest(String pin) {
        public String getPin() { return pin; }
    }

    // ── Fleets ──
    @PostMapping("/fleets")
    public ResponseEntity<Fleet> createFleet(@RequestBody Fleet f) {
        if (f.getFleetId() == null) f.setFleetId("FL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return ResponseEntity.ok(fleetRepo.save(f));
    }
    @GetMapping("/fleets")
    public ResponseEntity<List<Fleet>> getFleets() { return ResponseEntity.ok(fleetRepo.findAll()); }
    @PutMapping("/fleets/{id}")
    public ResponseEntity<Fleet> updateFleet(@PathVariable String id, @RequestBody Fleet f) { f.setFleetId(id); return ResponseEntity.ok(fleetRepo.save(f)); }
    @DeleteMapping("/fleets/{id}")
    public ResponseEntity<Void> deleteFleet(@PathVariable String id) { fleetRepo.deleteById(id); return ResponseEntity.noContent().build(); }

    // ── Operating Pincodes ──
    @PostMapping("/operating-pincodes")
    public ResponseEntity<OperatingPincode> createOpPincode(@RequestBody OperatingPincode p) { return ResponseEntity.ok(opPincodeRepo.save(p)); }
    @GetMapping("/operating-pincodes")
    public ResponseEntity<List<OperatingPincode>> getOpPincodes() { return ResponseEntity.ok(opPincodeRepo.findAll()); }
    @DeleteMapping("/operating-pincodes/{id}")
    public ResponseEntity<Void> deleteOpPincode(@PathVariable Long id) { opPincodeRepo.deleteById(id); return ResponseEntity.noContent().build(); }

    // ── Booking Pincodes ──
    @PostMapping("/booking-pincodes")
    public ResponseEntity<BookingPincode> createBkPincode(@RequestBody BookingPincode p) { return ResponseEntity.ok(bkPincodeRepo.save(p)); }
    @GetMapping("/booking-pincodes")
    public ResponseEntity<List<BookingPincode>> getBkPincodes() { return ResponseEntity.ok(bkPincodeRepo.findAll()); }
    @DeleteMapping("/booking-pincodes/{id}")
    public ResponseEntity<Void> deleteBkPincode(@PathVariable Long id) { bkPincodeRepo.deleteById(id); return ResponseEntity.noContent().build(); }

    // ── Parties (Source / Destination) ──
    @PostMapping("/parties")
    public ResponseEntity<Party> createParty(@RequestBody Party p) {
        if (p.getPartyId() == null) p.setPartyId(p.getPartyRole().substring(0, 2).toUpperCase() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return ResponseEntity.ok(partyRepo.save(p));
    }
    @GetMapping("/parties")
    public ResponseEntity<List<Party>> getParties(@RequestParam(required = false) String role) {
        return ResponseEntity.ok(role != null ? partyRepo.findByPartyRole(role) : partyRepo.findAll());
    }
    @PutMapping("/parties/{id}")
    public ResponseEntity<Party> updateParty(@PathVariable String id, @RequestBody Party p) { p.setPartyId(id); return ResponseEntity.ok(partyRepo.save(p)); }
    @DeleteMapping("/parties/{id}")
    public ResponseEntity<Void> deleteParty(@PathVariable String id) { partyRepo.deleteById(id); return ResponseEntity.noContent().build(); }

    // ── Sites ──
    @PostMapping("/sites")
    public ResponseEntity<Site> createSite(@RequestBody Site s) {
        if (s.getSiteId() == null) s.setSiteId("ST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return ResponseEntity.ok(siteRepo.save(s));
    }
    @GetMapping("/sites")
    public ResponseEntity<List<Site>> getSites() { return ResponseEntity.ok(siteRepo.findAll()); }
    @PutMapping("/sites/{id}")
    public ResponseEntity<Site> updateSite(@PathVariable String id, @RequestBody Site s) { s.setSiteId(id); return ResponseEntity.ok(siteRepo.save(s)); }
    @DeleteMapping("/sites/{id}")
    public ResponseEntity<Void> deleteSite(@PathVariable String id) { siteRepo.deleteById(id); return ResponseEntity.noContent().build(); }
}
