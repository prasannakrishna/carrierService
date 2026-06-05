-- V2__create_volumetric_tables.sql

CREATE TABLE vehicle_capacities (
    capacity_id            VARCHAR(50)    PRIMARY KEY,
    vehicle_type           VARCHAR(50)    NOT NULL,
    vehicle_sub_type       VARCHAR(50),
    max_payload_kg         NUMERIC(12,2),
    tare_weight_kg         NUMERIC(12,2),
    gross_vehicle_weight_kg NUMERIC(12,2),
    internal_length_m      NUMERIC(8,3),
    internal_width_m       NUMERIC(8,3),
    internal_height_m      NUMERIC(8,3),
    max_volume_m3          NUMERIC(10,4),
    volumetric_divisor     INT DEFAULT 4000,
    max_pallets            INT,
    max_packages           INT,
    door_width_m           NUMERIC(6,3),
    door_height_m          NUMERIC(6,3),
    utilization_factor_pct NUMERIC(5,2) DEFAULT 85.00,
    status                 VARCHAR(20) DEFAULT 'Active'
);

CREATE TABLE shipment_volumetrics (
    id                  BIGSERIAL      PRIMARY KEY,
    rts_id              VARCHAR(100),
    shipment_id         VARCHAR(100),
    total_packages      INT,
    weight_unit         VARCHAR(10),
    gross_weight        NUMERIC(12,3),
    net_weight          NUMERIC(12,3),
    volume_unit         VARCHAR(10),
    gross_volume        NUMERIC(12,4),
    dimension_unit      VARCHAR(10),
    total_length        NUMERIC(10,3),
    total_width         NUMERIC(10,3),
    total_height        NUMERIC(10,3),
    volumetric_weight   NUMERIC(12,3),
    chargeable_weight   NUMERIC(12,3),
    volume_weight_ratio NUMERIC(6,3),
    pack_method         VARCHAR(30),
    pallet_count        INT,
    stackable           BOOLEAN
);

CREATE INDEX idx_vol_rts ON shipment_volumetrics(rts_id);
CREATE INDEX idx_vol_shipment ON shipment_volumetrics(shipment_id);

-- Seed: Standard Indian vehicle types
INSERT INTO vehicle_capacities (capacity_id, vehicle_type, vehicle_sub_type, max_payload_kg, tare_weight_kg, gross_vehicle_weight_kg, internal_length_m, internal_width_m, internal_height_m, max_volume_m3, volumetric_divisor, max_pallets, utilization_factor_pct, status) VALUES
('VC-TATA-ACE',   'TATA_ACE',       'CLOSED',       750,    1200,  1950,  2.100, 1.500, 1.500,  4.7250, 4000, 2,  80.00, 'Active'),
('VC-PICKUP',     'PICKUP_TRUCK',   'OPEN',         1000,   1500,  2500,  2.400, 1.600, 1.200,  4.6080, 4000, 2,  75.00, 'Active'),
('VC-14FT',       'TRUCK_14FT',     'CLOSED',       4000,   3500,  7500,  4.270, 2.130, 2.130, 19.3700, 4000, 6,  85.00, 'Active'),
('VC-17FT',       'TRUCK_17FT',     'CLOSED',       5500,   4000,  9500,  5.180, 2.290, 2.290, 27.1600, 4000, 8,  85.00, 'Active'),
('VC-20FT',       'TRUCK_20FT',     'CLOSED',       7000,   5000, 12000,  6.100, 2.440, 2.440, 36.3000, 4000, 10, 85.00, 'Active'),
('VC-22FT',       'TRUCK_22FT',     'CLOSED',       9000,   6000, 15000,  6.700, 2.440, 2.440, 39.8600, 4000, 11, 85.00, 'Active'),
('VC-32FT',       'TRUCK_32FT',     'CLOSED',      15000,   9000, 25000,  9.750, 2.440, 2.440, 58.0000, 4000, 16, 85.00, 'Active'),
('VC-TRAILER',    'TRAILER_40FT',   'CLOSED',      25000,  12000, 37000, 12.190, 2.440, 2.590, 76.9000, 4000, 22, 85.00, 'Active'),
('VC-CONTAINER20','CONTAINER_20FT', 'CLOSED',      21770,   2300, 24000,  5.900, 2.350, 2.390, 33.2000, 1000, 10, 85.00, 'Active'),
('VC-CONTAINER40','CONTAINER_40FT', 'CLOSED',      26580,   3750, 30480, 12.030, 2.350, 2.390, 67.7000, 1000, 22, 85.00, 'Active'),
('VC-REEFER20',   'REEFER_20FT',    'REFRIGERATED',21000,   3000, 24000,  5.440, 2.290, 2.270, 28.3000, 1000, 9,  80.00, 'Active'),
('VC-VAN',        'VAN',            'CLOSED',       500,    1800,  2300,  1.800, 1.200, 1.200,  2.5920, 4000, 1,  80.00, 'Active');

-- ISO Sea Containers (standard dimensions per ISO 668)
INSERT INTO vehicle_capacities (capacity_id, vehicle_type, vehicle_sub_type, max_payload_kg, tare_weight_kg, gross_vehicle_weight_kg, internal_length_m, internal_width_m, internal_height_m, max_volume_m3, volumetric_divisor, max_pallets, utilization_factor_pct, status) VALUES
('VC-22G0', 'CONTAINER_20FT_STD',    'STANDARD_DRY',   21770, 2300, 24000, 5.900, 2.350, 2.390, 33.2000, 1000, 10, 85.00, 'Active'),
('VC-42G0', 'CONTAINER_40FT_STD',    'STANDARD_DRY',   26580, 3750, 30480, 12.030, 2.350, 2.390, 67.7000, 1000, 22, 85.00, 'Active'),
('VC-45G0', 'CONTAINER_45FT_STD',    'STANDARD_DRY',   25600, 4800, 30400, 13.556, 2.352, 2.385, 76.0000, 1000, 26, 85.00, 'Active'),
('VC-25GP', 'CONTAINER_20FT_HC',     'HIGH_CUBE',      21570, 2430, 24000, 5.900, 2.350, 2.690, 37.4000, 1000, 10, 85.00, 'Active'),
('VC-45GP', 'CONTAINER_40FT_HC',     'HIGH_CUBE',      26330, 4150, 30480, 12.030, 2.350, 2.690, 76.2000, 1000, 22, 85.00, 'Active'),
('VC-L5GP', 'CONTAINER_45FT_HC',     'HIGH_CUBE',      25400, 5000, 30400, 13.556, 2.352, 2.690, 85.9000, 1000, 26, 85.00, 'Active'),
('VC-22R1', 'CONTAINER_20FT_REEFER', 'REFRIGERATED',   21000, 3000, 24000, 5.440, 2.290, 2.270, 28.3000, 1000, 9,  80.00, 'Active'),
('VC-42R1', 'CONTAINER_40FT_REEFER', 'REFRIGERATED',   26280, 4200, 30480, 11.560, 2.290, 2.250, 59.7000, 1000, 20, 80.00, 'Active'),
('VC-45R1', 'CONTAINER_45FT_REEFER', 'REFRIGERATED',   25200, 5200, 30400, 13.100, 2.290, 2.250, 67.5000, 1000, 24, 80.00, 'Active'),
('VC-22U1', 'CONTAINER_20FT_OT',    'OPEN_TOP',       21680, 2320, 24000, 5.890, 2.345, 2.315, 32.0000, 1000, 10, 75.00, 'Active'),
('VC-42U1', 'CONTAINER_40FT_OT',    'OPEN_TOP',       26480, 4000, 30480, 12.030, 2.345, 2.315, 65.5000, 1000, 22, 75.00, 'Active'),
('VC-22P1', 'CONTAINER_20FT_FR',    'FLAT_RACK',      21700, 2300, 24000, 5.620, 2.200, 2.230, 27.6000, 1000, 8,  70.00, 'Active'),
('VC-42P1', 'CONTAINER_40FT_FR',    'FLAT_RACK',      39200, 5000, 44200, 12.080, 2.240, 2.030, 54.9000, 1000, 20, 70.00, 'Active'),
('VC-22T0', 'CONTAINER_20FT_TANK',  'TANK',           21000, 3070, 24000, 6.058, 2.438, 2.438, 24.0000, 1000, 0,  95.00, 'Active'),
('VC-22B0', 'CONTAINER_20FT_BULK',  'BULK',           21600, 2400, 24000, 5.900, 2.350, 2.390, 33.2000, 1000, 0,  90.00, 'Active');

-- Air Freight ULDs (Unit Load Devices — IATA standard)
INSERT INTO vehicle_capacities (capacity_id, vehicle_type, vehicle_sub_type, max_payload_kg, tare_weight_kg, gross_vehicle_weight_kg, internal_length_m, internal_width_m, internal_height_m, max_volume_m3, volumetric_divisor, max_pallets, utilization_factor_pct, status) VALUES
('VC-AKE',  'ULD_AKE_LD3',     'AIR_CONTOURED',   1588, 80,  1668, 1.534, 1.562, 1.143, 3.500, 5000, 0, 80.00, 'Active'),
('VC-AKC',  'ULD_AKC_LD3',     'AIR_CONTOURED',   1588, 71,  1659, 1.534, 1.562, 1.143, 3.500, 5000, 0, 80.00, 'Active'),
('VC-AKN',  'ULD_AKN_LD3',     'AIR_CONTOURED',   1588, 100, 1688, 1.534, 1.562, 1.143, 3.500, 5000, 0, 80.00, 'Active'),
('VC-ALF',  'ULD_ALF_LD6',     'AIR_CONTOURED',   3175, 145, 3320, 3.175, 1.534, 1.143, 5.800, 5000, 0, 80.00, 'Active'),
('VC-AMA',  'ULD_AMA_LD7',     'AIR_CONTOURED',   4626, 120, 4746, 3.175, 2.235, 1.143, 8.100, 5000, 0, 80.00, 'Active'),
('VC-AAP',  'ULD_AAP_LD9',     'AIR_CONTOURED',   6033, 120, 6153, 3.175, 2.235, 1.524, 10.800, 5000, 0, 80.00, 'Active'),
('VC-PMC',  'ULD_PMC_P6',      'AIR_PALLET',      4500, 110, 4610, 3.175, 2.438, 1.625, 12.600, 5000, 0, 75.00, 'Active'),
('VC-PAG',  'ULD_PAG_P1',      'AIR_PALLET',      6800, 120, 6920, 6.058, 2.438, 1.625, 24.000, 5000, 0, 75.00, 'Active'),
('VC-RKN',  'ULD_RKN_COOL',    'AIR_REFRIGERATED',1300, 288, 1588, 1.534, 1.562, 1.143, 2.700, 5000, 0, 75.00, 'Active'),
('VC-RAP',  'ULD_RAP_COOL',    'AIR_REFRIGERATED',4200, 433, 4633, 3.175, 2.235, 1.524, 8.500, 5000, 0, 75.00, 'Active');

-- Flatbed Trailers (Road — open cargo)
INSERT INTO vehicle_capacities (capacity_id, vehicle_type, vehicle_sub_type, max_payload_kg, tare_weight_kg, gross_vehicle_weight_kg, internal_length_m, internal_width_m, internal_height_m, max_volume_m3, volumetric_divisor, max_pallets, utilization_factor_pct, status) VALUES
('VC-FB20', 'FLATBED_20FT',    'OPEN',            18000, 2500, 20500, 6.100, 2.440, 2.600, 38.7000, 4000, 10, 70.00, 'Active'),
('VC-FB40', 'FLATBED_40FT',    'OPEN',            28000, 5000, 33000, 12.190, 2.440, 2.600, 77.3000, 4000, 22, 70.00, 'Active');
