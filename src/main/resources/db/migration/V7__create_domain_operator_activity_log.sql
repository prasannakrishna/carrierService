-- Domain Operator Activity Log: per-service operational activity tracking

CREATE TABLE domain_operator_activity_log (
    activity_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    operator_id       UUID NOT NULL,
    facility_id       UUID NOT NULL,
    action_type       VARCHAR(50) NOT NULL,
    entity_type       VARCHAR(50) NOT NULL,
    entity_id         UUID NOT NULL,
    action_metadata   JSONB NOT NULL DEFAULT '{}',
    outcome           VARCHAR(10) NOT NULL DEFAULT 'SUCCESS',
    session_id        UUID NOT NULL,
    performed_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_activity_operator_time ON domain_operator_activity_log(operator_id, performed_at DESC);
CREATE INDEX idx_activity_facility_time ON domain_operator_activity_log(facility_id, performed_at DESC);
CREATE INDEX idx_activity_action_type ON domain_operator_activity_log(action_type, performed_at DESC);
