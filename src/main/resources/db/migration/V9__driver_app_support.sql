-- Driver mobile app support: PIN auth on drivers, consignments (destination-level
-- units within a transport shipment), delivery reports (mutual attestation), and
-- driver-raised transport exceptions.

ALTER TABLE drivers ADD COLUMN pin_hash VARCHAR(255);

-- Idempotency for offline-first mobile clients retrying a milestone post after a
-- delayed sync (carrier-driver-app queues writes in Room/WorkManager).
ALTER TABLE shipment_milestones ADD COLUMN idempotency_key VARCHAR(100);
CREATE UNIQUE INDEX uq_shipment_milestones_idempotency ON shipment_milestones(idempotency_key) WHERE idempotency_key IS NOT NULL;

CREATE TABLE consignments (
    consignment_id       VARCHAR(100) PRIMARY KEY,
    transport_shipment_id VARCHAR(100) NOT NULL REFERENCES transport_shipments(ts_id),
    order_id             VARCHAR(100),
    destination_type     VARCHAR(30) NOT NULL,
    destination_id       VARCHAR(100) NOT NULL,
    destination_name     VARCHAR(255),
    label_code           VARCHAR(50) NOT NULL UNIQUE,
    status               VARCHAR(20) NOT NULL DEFAULT 'MANIFESTED',
    total_packages       INTEGER DEFAULT 0,
    total_weight_kg      NUMERIC(12,3) DEFAULT 0,
    created_at           TIMESTAMP NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP
);

CREATE INDEX idx_consignments_ts ON consignments(transport_shipment_id);

CREATE TABLE consignment_items (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    consignment_id  VARCHAR(100) NOT NULL REFERENCES consignments(consignment_id) ON DELETE CASCADE,
    sku_id          VARCHAR(100) NOT NULL,
    product_name    VARCHAR(255),
    quantity        INTEGER DEFAULT 0,
    weight_kg       NUMERIC(12,3) DEFAULT 0
);

CREATE INDEX idx_consignment_items_consignment ON consignment_items(consignment_id);

CREATE TABLE delivery_reports (
    report_id           VARCHAR(100) PRIMARY KEY,
    consignment_id      VARCHAR(100) NOT NULL REFERENCES consignments(consignment_id),
    count_reported      INTEGER DEFAULT 0,
    condition_notes     TEXT,
    photos              TEXT,
    submitted_by        VARCHAR(100),
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING_APPROVAL',
    approval_deadline   TIMESTAMP,
    approved_at         TIMESTAMP,
    approved_by         VARCHAR(100),
    idempotency_key     VARCHAR(100) NOT NULL UNIQUE,
    created_at          TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_delivery_reports_consignment ON delivery_reports(consignment_id);

-- Driver-raised field exceptions (carrier-driver-app). Deliberately separate from
-- the pre-existing transport_exceptions table (LogisticsOpsController), which is
-- a different, unrelated concept with its own shape.
CREATE TABLE driver_field_exceptions (
    field_exception_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ts_id             VARCHAR(100) NOT NULL REFERENCES transport_shipments(ts_id),
    consignment_id    VARCHAR(100),
    exception_type    VARCHAR(50) NOT NULL,
    notes             TEXT,
    photos            TEXT,
    latitude          NUMERIC(9,6),
    longitude         NUMERIC(9,6),
    raised_by         VARCHAR(100),
    created_at        TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_driver_field_exceptions_ts ON driver_field_exceptions(ts_id);
