-- V1__create_logistics_maintenance_tables.sql

CREATE TABLE drivers (
    driver_id   VARCHAR(50)  PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    license_no  VARCHAR(50)  UNIQUE,
    contact     VARCHAR(20),
    fleet_id    VARCHAR(50),
    status      VARCHAR(30),
    created_at  TIMESTAMPTZ
);

CREATE TABLE fleets (
    fleet_id      VARCHAR(50)  PRIMARY KEY,
    fleet_name    VARCHAR(200) NOT NULL,
    manager       VARCHAR(200),
    vehicle_count INT,
    region        VARCHAR(100),
    status        VARCHAR(30),
    created_at    TIMESTAMPTZ
);

CREATE TABLE operating_pincodes (
    id       BIGSERIAL    PRIMARY KEY,
    pincode  VARCHAR(10)  NOT NULL,
    city     VARCHAR(100),
    state    VARCHAR(100),
    zone     VARCHAR(50),
    status   VARCHAR(30)
);

CREATE TABLE booking_pincodes (
    id          BIGSERIAL    PRIMARY KEY,
    pincode     VARCHAR(10)  NOT NULL,
    city        VARCHAR(100),
    state       VARCHAR(100),
    carrier     VARCHAR(100),
    cutoff_time VARCHAR(20)
);

CREATE TABLE parties (
    party_id   VARCHAR(50)  PRIMARY KEY,
    name       VARCHAR(200) NOT NULL,
    party_role VARCHAR(30),
    type       VARCHAR(50),
    city       VARCHAR(100),
    contact    VARCHAR(50),
    status     VARCHAR(30),
    created_at TIMESTAMPTZ
);

CREATE TABLE sites (
    site_id  VARCHAR(50)  PRIMARY KEY,
    name     VARCHAR(200) NOT NULL,
    type     VARCHAR(50),
    city     VARCHAR(100),
    capacity VARCHAR(50),
    status   VARCHAR(30)
);

CREATE TABLE transport_exceptions (
    exception_id VARCHAR(50)  PRIMARY KEY,
    shipment_id  VARCHAR(50),
    type         VARCHAR(50),
    priority     VARCHAR(20),
    description  TEXT,
    raised_by    VARCHAR(100),
    status       VARCHAR(30),
    created_at   TIMESTAMPTZ
);

CREATE TABLE issues (
    issue_id    VARCHAR(50)  PRIMARY KEY,
    shipment_id VARCHAR(50),
    priority    VARCHAR(20),
    description TEXT,
    assigned_to VARCHAR(100),
    status      VARCHAR(30),
    created_at  TIMESTAMPTZ
);

CREATE TABLE freight_invoices (
    invoice_id VARCHAR(50)    PRIMARY KEY,
    party      VARCHAR(200),
    amount     NUMERIC(14,2),
    type       VARCHAR(30),
    due_date   DATE,
    status     VARCHAR(30),
    created_at TIMESTAMPTZ
);

CREATE TABLE payments (
    payment_id VARCHAR(50)    PRIMARY KEY,
    invoice_id VARCHAR(50),
    party      VARCHAR(200),
    amount     NUMERIC(14,2),
    method     VARCHAR(50),
    status     VARCHAR(30),
    created_at TIMESTAMPTZ
);

CREATE TABLE rate_cards (
    rate_card_id VARCHAR(50)    PRIMARY KEY,
    route        VARCHAR(200),
    vehicle_type VARCHAR(50),
    rate_per_km  NUMERIC(10,2),
    min_weight   VARCHAR(50),
    status       VARCHAR(30)
);

CREATE TABLE logistics_contracts (
    contract_id VARCHAR(50)    PRIMARY KEY,
    party       VARCHAR(200),
    party_type  VARCHAR(50),
    start_date  DATE,
    end_date    DATE,
    value       NUMERIC(14,2),
    status      VARCHAR(30)
);

CREATE TABLE vehicle_configs (
    config_id    VARCHAR(50)    PRIMARY KEY,
    vehicle_type VARCHAR(50),
    max_load_kg  NUMERIC(12,2),
    dimensions   VARCHAR(100),
    fuel_type    VARCHAR(30),
    status       VARCHAR(30)
);

CREATE TABLE notification_configs (
    config_id    VARCHAR(50)  PRIMARY KEY,
    event        VARCHAR(200),
    channel      VARCHAR(100),
    recipients   VARCHAR(255),
    trigger_rule VARCHAR(255),
    status       VARCHAR(30)
);

CREATE TABLE user_groups (
    group_id     VARCHAR(50)  PRIMARY KEY,
    group_name   VARCHAR(200) NOT NULL,
    permissions  VARCHAR(500),
    member_count INT,
    status       VARCHAR(30)
);

CREATE TABLE integration_configs (
    integration_id VARCHAR(50)  PRIMARY KEY,
    system_name    VARCHAR(200),
    type           VARCHAR(30),
    direction      VARCHAR(30),
    last_sync      TIMESTAMPTZ,
    status         VARCHAR(30)
);

CREATE INDEX idx_drivers_fleet    ON drivers(fleet_id);
CREATE INDEX idx_drivers_status   ON drivers(status);
CREATE INDEX idx_exceptions_status ON transport_exceptions(status);
CREATE INDEX idx_issues_status    ON issues(status);
CREATE INDEX idx_parties_role     ON parties(party_role);
