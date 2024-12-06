package com.bhagwat.scm.carrierService.controller;

import com.bhagwat.scm.carrierService.entity.FirstMileCarrier;
import com.bhagwat.scm.carrierService.service.FirstMileCarrierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/first-mile-carriers")
public class FirstMileCarrierController {

    @Autowired
    private FirstMileCarrierService firstMileCarrierService;

    @PostMapping
    public ResponseEntity<FirstMileCarrier> createCarrier(@RequestBody FirstMileCarrier carrier) {
        return ResponseEntity.ok(firstMileCarrierService.createCarrier(carrier));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FirstMileCarrier> updateCarrier(@PathVariable Long id, @RequestBody FirstMileCarrier carrier) {
        return ResponseEntity.ok(firstMileCarrierService.updateCarrier(id, carrier));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FirstMileCarrier> getCarrierById(@PathVariable Long id) {
        return ResponseEntity.ok(firstMileCarrierService.getCarrierById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCarrier(@PathVariable Long id) {
        firstMileCarrierService.deleteCarrier(id);
        return ResponseEntity.noContent().build();
    }
}

