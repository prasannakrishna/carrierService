package com.bhagwat.scm.carrierService.service;

import com.bhagwat.scm.carrierService.entity.LastMileCarrier;
import com.bhagwat.scm.carrierService.repository.LastMileCarrierRepository;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LastMileCarrierService {

    @Autowired
    private LastMileCarrierRepository lastMileCarrierRepository;

    public LastMileCarrier createCarrier(LastMileCarrier carrier) {
        return lastMileCarrierRepository.save(carrier);
    }

    public LastMileCarrier updateCarrier(Long id, LastMileCarrier carrier) {
        LastMileCarrier existingCarrier = lastMileCarrierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carrier not found"));
        existingCarrier.setCarrierName(carrier.getCarrierName());
        existingCarrier.setOperatingPincodes(carrier.getOperatingPincodes());
        existingCarrier.setCarrierAddress(carrier.getCarrierAddress());
        return lastMileCarrierRepository.save(existingCarrier);
    }

    public LastMileCarrier getCarrierById(Long id) {
        return lastMileCarrierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carrier not found"));
    }

    public void deleteCarrier(Long id) {
        lastMileCarrierRepository.deleteById(id);
    }
}

