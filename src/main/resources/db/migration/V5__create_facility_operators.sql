-- Federated Operator Identity: facility_operators table
-- Each domain service (wmsService, storeService, carrierService) owns its operator records

CREATE TABLE facility_operators (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owning_service        VARCHAR(20) NOT NULL,
    facility_owner_org_id UUID NOT NULL,
    operator_name         VARCHAR(128) NOT NULL,
    pin_hash              VARCHAR(255) NOT NULL,
    pin_last_changed_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    status                VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    phone_number          VARCHAR(15),
    vehicle_type          VARCHAR(50),
    created_by            UUID NOT NULL,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_operator_name_per_org UNIQUE (owning_service, facility_owner_org_id, operator_name)
);

CREATE INDEX idx_facility_operators_org ON facility_operators(facility_owner_org_id, status);
CREATE INDEX idx_facility_operators_phone ON facility_operators(phone_number) WHERE phone_number IS NOT NULL;

-- PIN history for rotation policy (last 3 retained)
CREATE TABLE pin_history (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    operator_id   UUID NOT NULL REFERENCES facility_operators(id) ON DELETE CASCADE,
    pin_hash      VARCHAR(255) NOT NULL,
    changed_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_pin_history_operator ON pin_history(operator_id, changed_at DESC);
