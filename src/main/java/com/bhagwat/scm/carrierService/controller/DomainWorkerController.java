package com.bhagwat.scm.carrierService.controller;

import com.bhagwat.scm.carrierService.entity.DomainRole;
import com.bhagwat.scm.carrierService.entity.DomainWorker;
import com.bhagwat.scm.carrierService.repository.DomainRoleRepository;
import com.bhagwat.scm.carrierService.repository.DomainWorkerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/carrier/domain-workers")
@RequiredArgsConstructor
@Tag(name = "Domain Workers", description = "Manage domain-specific operators and managers (mobile app users)")
public class DomainWorkerController {

    private final DomainRoleRepository roleRepo;
    private final DomainWorkerRepository workerRepo;

    // ── Roles ────────────────────────────────────────────────────────────────

    @PostMapping("/roles")
    @Operation(summary = "Create a domain role (e.g. Driver, Dispatcher)")
    public ResponseEntity<DomainRole> createRole(@RequestBody DomainRole role) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleRepo.save(role));
    }

    @GetMapping("/roles")
    @Operation(summary = "List all domain roles")
    public ResponseEntity<List<DomainRole>> listRoles(@RequestParam(required = false) String roleType) {
        if (roleType != null) return ResponseEntity.ok(roleRepo.findByRoleType(roleType));
        return ResponseEntity.ok(roleRepo.findByIsActiveTrue());
    }

    @PutMapping("/roles/{roleId}")
    @Operation(summary = "Update a domain role")
    public ResponseEntity<DomainRole> updateRole(@PathVariable String roleId, @RequestBody DomainRole role) {
        role.setRoleId(roleId);
        return ResponseEntity.ok(roleRepo.save(role));
    }

    @DeleteMapping("/roles/{roleId}")
    @Operation(summary = "Deactivate a domain role")
    public ResponseEntity<Void> deactivateRole(@PathVariable String roleId) {
        roleRepo.findById(roleId).ifPresent(r -> { r.setIsActive(false); roleRepo.save(r); });
        return ResponseEntity.noContent().build();
    }

    // ── Workers ──────────────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Create a domain worker (operator/manager for mobile app)")
    public ResponseEntity<DomainWorker> createWorker(@RequestBody DomainWorker worker) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workerRepo.save(worker));
    }

    @GetMapping
    @Operation(summary = "List all domain workers")
    public ResponseEntity<List<DomainWorker>> listWorkers(
            @RequestParam(required = false) String roleId,
            @RequestParam(required = false) String status) {
        if (roleId != null) return ResponseEntity.ok(workerRepo.findByRoleId(roleId));
        if (status != null) return ResponseEntity.ok(workerRepo.findByStatus(status));
        return ResponseEntity.ok(workerRepo.findAll());
    }

    @GetMapping("/{workerId}")
    @Operation(summary = "Get worker by ID")
    public ResponseEntity<DomainWorker> getWorker(@PathVariable String workerId) {
        return workerRepo.findById(workerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{workerId}")
    @Operation(summary = "Update a domain worker")
    public ResponseEntity<DomainWorker> updateWorker(@PathVariable String workerId, @RequestBody DomainWorker worker) {
        worker.setWorkerId(workerId);
        return ResponseEntity.ok(workerRepo.save(worker));
    }

    @DeleteMapping("/{workerId}")
    @Operation(summary = "Deactivate a domain worker")
    public ResponseEntity<Void> deactivateWorker(@PathVariable String workerId) {
        workerRepo.findById(workerId).ifPresent(w -> { w.setStatus("INACTIVE"); workerRepo.save(w); });
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{workerId}/pin")
    @Operation(summary = "Set or reset worker's mobile app PIN")
    public ResponseEntity<Map<String, String>> setPin(@PathVariable String workerId, @RequestBody Map<String, String> body) {
        DomainWorker w = workerRepo.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));
        w.setPin(body.get("pin"));
        workerRepo.save(w);
        return ResponseEntity.ok(Map.of("message", "PIN updated", "workerId", workerId));
    }

    // ── Mobile App Auth ──────────────────────────────────────────────────────

    @PostMapping("/auth/login")
    @Operation(summary = "Mobile app login — authenticate worker by phone + PIN")
    public ResponseEntity<?> mobileLogin(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String pin = body.get("pin");

        if (phone == null || pin == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "phone and pin required"));
        }

        return workerRepo.findByPhoneAndPin(phone, pin)
                .filter(w -> "ACTIVE".equals(w.getStatus()))
                .map(w -> {
                    w.setLastLoginAt(LocalDateTime.now());
                    workerRepo.save(w);
                    // Return worker info + role (mobile app uses this to show relevant screens)
                    DomainRole role = roleRepo.findById(w.getRoleId()).orElse(null);
                    return ResponseEntity.ok(Map.of(
                            "workerId", w.getWorkerId(),
                            "name", w.getName(),
                            "phone", w.getPhone(),
                            "roleId", w.getRoleId(),
                            "roleName", role != null ? role.getRoleName() : "",
                            "roleType", role != null ? role.getRoleType() : "",
                            "permissions", role != null && role.getPermissions() != null ? role.getPermissions() : "[]",
                            "status", w.getStatus()
                    ));
                })
                .orElse(ResponseEntity.status(401).body(Map.of("error", "Invalid credentials or inactive account")));
    }
}
