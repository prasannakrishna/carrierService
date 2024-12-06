package com.bhagwat.scm.carrierService.service;

import com.bhagwat.scm.carrierService.entity.FirstMileCarrier;
import com.bhagwat.scm.carrierService.repository.FirstMileCarrierRepository;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FirstMileCarrierService {

    @Autowired
    private FirstMileCarrierRepository firstMileCarrierRepository;

    public FirstMileCarrier createCarrier(FirstMileCarrier carrier) {
        return firstMileCarrierRepository.save(carrier);
    }

    public FirstMileCarrier updateCarrier(Long id, FirstMileCarrier carrier) {
        FirstMileCarrier existingCarrier = firstMileCarrierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carrier not found"));
        existingCarrier.setCarrierName(carrier.getCarrierName());
        existingCarrier.setOperatingPincodes(carrier.getOperatingPincodes());
        existingCarrier.setCarrierAddress(carrier.getCarrierAddress());
        return firstMileCarrierRepository.save(existingCarrier);
    }

    public FirstMileCarrier getCarrierById(Long id) {
        return firstMileCarrierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carrier not found"));
    }

    public void deleteCarrier(Long id) {
        firstMileCarrierRepository.deleteById(id);
    }
}

