package com.bhagwat.scm.carrierService.service;

import com.bhagwat.scm.carrierService.dto.*;
import com.bhagwat.scm.carrierService.entity.*;
import com.bhagwat.scm.carrierService.enums.VehicleStatus;
import com.bhagwat.scm.carrierService.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class CarrierMasterService {

    private final CarrierRepository carrierRepository;
    private final CarrierVehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    @Transactional
    public CarrierResponse createCarrier(CarrierRequest req) {
        Carrier c = Carrier.builder()
                .carrierName(req.getCarrierName())
                .scacCode(req.getScacCode())
                .carrierType(req.getCarrierType())
                .contactEmail(req.getContactEmail())
                .contactPhone(req.getContactPhone())
                .gstin(req.getGstin())
                .panNumber(req.getPanNumber())
                .address(toAddress(req.getAddress()))
                .active(true)
                .build();
        return toCarrierResponse(carrierRepository.save(c));
    }

    @Transactional(readOnly = true)
    public CarrierResponse getCarrier(String carrierId) {
        return toCarrierResponse(find(carrierId));
    }

    @Transactional(readOnly = true)
    public List<CarrierResponse> listCarriers() {
        return carrierRepository.findAll().stream().map(this::toCarrierResponse).collect(Collectors.toList());
    }

    @Transactional
    public CarrierResponse updateCarrier(String carrierId, CarrierRequest req) {
        Carrier c = find(carrierId);
        c.setCarrierName(req.getCarrierName());
        c.setScacCode(req.getScacCode());
        c.setCarrierType(req.getCarrierType());
        c.setContactEmail(req.getContactEmail());
        c.setContactPhone(req.getContactPhone());
        c.setGstin(req.getGstin());
        c.setPanNumber(req.getPanNumber());
        c.setAddress(toAddress(req.getAddress()));
        return toCarrierResponse(carrierRepository.save(c));
    }

    @Transactional
    public CarrierVehicleResponse addVehicle(CarrierVehicleRequest req) {
        CarrierVehicle v = CarrierVehicle.builder()
                .carrierId(req.getCarrierId())
                .fleetId(req.getFleetId())
                .vehicleNumber(req.getVehicleNumber())
                .vehicleType(req.getVehicleType())
                .capacityKg(req.getCapacityKg())
                .volumeCapacityCbm(req.getVolumeCapacityCbm())
                .driverName(req.getDriverName())
                .driverPhone(req.getDriverPhone())
                .driverLicense(req.getDriverLicense())
                .status(VehicleStatus.AVAILABLE)
                .build();
        return toVehicleResponse(vehicleRepository.save(v));
    }

    @Transactional(readOnly = true)
    public List<CarrierVehicleResponse> getVehicles(String carrierId) {
        return vehicleRepository.findByCarrierId(carrierId).stream().map(this::toVehicleResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CarrierVehicleResponse> getAllVehicles() {
        return vehicleRepository.findAll().stream().map(this::toVehicleResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CarrierVehicleResponse getVehicleById(String vehicleId) {
        CarrierVehicle v = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + vehicleId));
        return toVehicleResponse(v);
    }

    @Transactional
    public CarrierVehicleResponse updateVehicle(String vehicleId, CarrierVehicleRequest req) {
        CarrierVehicle v = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + vehicleId));
        v.setCarrierId(req.getCarrierId());
        v.setVehicleNumber(req.getVehicleNumber());
        if (req.getVehicleType() != null) v.setVehicleType(req.getVehicleType());
        if (req.getCapacityKg() != null) v.setCapacityKg(req.getCapacityKg());
        if (req.getVolumeCapacityCbm() != null) v.setVolumeCapacityCbm(req.getVolumeCapacityCbm());
        if (req.getDriverName() != null) v.setDriverName(req.getDriverName());
        if (req.getDriverPhone() != null) v.setDriverPhone(req.getDriverPhone());
        if (req.getDriverLicense() != null) v.setDriverLicense(req.getDriverLicense());
        return toVehicleResponse(vehicleRepository.save(v));
    }

    @Transactional
    public void deleteVehicle(String vehicleId) {
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new RuntimeException("Vehicle not found: " + vehicleId);
        }
        vehicleRepository.deleteById(vehicleId);
    }

    @Transactional
    public CarrierVehicleResponse updateVehicleStatus(String vehicleId, VehicleStatus status) {
        CarrierVehicle v = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + vehicleId));
        v.setStatus(status);
        return toVehicleResponse(vehicleRepository.save(v));
    }

    // ── Assignment Operations ────────────────────────────────────────────────

    @Transactional
    public CarrierVehicleResponse assignVehicleToFleet(String vehicleId, String fleetId) {
        CarrierVehicle v = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + vehicleId));
        v.setFleetId(fleetId);
        return toVehicleResponse(vehicleRepository.save(v));
    }

    @Transactional
    public CarrierVehicleResponse unassignVehicleFromFleet(String vehicleId) {
        CarrierVehicle v = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + vehicleId));
        if (v.getStatus() == VehicleStatus.ON_TRIP) {
            throw new RuntimeException("Cannot unassign vehicle from fleet — vehicle has an active shipment (status: ON_TRIP)");
        }
        v.setFleetId(null);
        return toVehicleResponse(vehicleRepository.save(v));
    }

    @Transactional
    public CarrierVehicleResponse assignDriverToVehicle(String vehicleId, String driverId) {
        CarrierVehicle v = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + vehicleId));
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found: " + driverId));
        v.setDriverId(driver.getDriverId());
        v.setDriverName(driver.getName());
        v.setDriverPhone(driver.getContact());
        v.setDriverLicense(driver.getLicenseNo());
        return toVehicleResponse(vehicleRepository.save(v));
    }

    @Transactional
    public CarrierVehicleResponse unassignDriverFromVehicle(String vehicleId) {
        CarrierVehicle v = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + vehicleId));
        if (v.getStatus() == VehicleStatus.ON_TRIP) {
            throw new RuntimeException("Cannot unassign driver — vehicle has an active shipment (status: ON_TRIP)");
        }
        v.setDriverId(null);
        v.setDriverName(null);
        v.setDriverPhone(null);
        v.setDriverLicense(null);
        return toVehicleResponse(vehicleRepository.save(v));
    }

    private Carrier find(String carrierId) {
        return carrierRepository.findById(carrierId)
                .orElseThrow(() -> new RuntimeException("Carrier not found: " + carrierId));
    }

    private LocationAddress toAddress(LocationAddressDto dto) {
        if (dto == null) return null;
        return LocationAddress.builder()
                .locationId(dto.getLocationId()).street(dto.getStreet())
                .city(dto.getCity()).state(dto.getState())
                .pincode(dto.getPincode()).country(dto.getCountry()).build();
    }

    private LocationAddressDto toAddressDto(LocationAddress a) {
        if (a == null) return null;
        return LocationAddressDto.builder()
                .locationId(a.getLocationId()).street(a.getStreet())
                .city(a.getCity()).state(a.getState())
                .pincode(a.getPincode()).country(a.getCountry()).build();
    }

    CarrierResponse toCarrierResponse(Carrier c) {
        return CarrierResponse.builder()
                .carrierId(c.getCarrierId()).carrierName(c.getCarrierName())
                .scacCode(c.getScacCode()).carrierType(c.getCarrierType())
                .contactEmail(c.getContactEmail()).contactPhone(c.getContactPhone())
                .address(toAddressDto(c.getAddress()))
                .gstin(c.getGstin()).panNumber(c.getPanNumber())
                .active(c.getActive()).createdAt(c.getCreatedAt()).build();
    }

    CarrierVehicleResponse toVehicleResponse(CarrierVehicle v) {
        return CarrierVehicleResponse.builder()
                .vehicleId(v.getVehicleId()).carrierId(v.getCarrierId())
                .fleetId(v.getFleetId())
                .vehicleNumber(v.getVehicleNumber()).vehicleType(v.getVehicleType())
                .capacityKg(v.getCapacityKg()).volumeCapacityCbm(v.getVolumeCapacityCbm())
                .status(v.getStatus()).driverName(v.getDriverName())
                .driverPhone(v.getDriverPhone()).driverLicense(v.getDriverLicense())
                .createdAt(v.getCreatedAt()).build();
    }
}
