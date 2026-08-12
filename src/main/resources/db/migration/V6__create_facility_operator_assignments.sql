-- Facility Operator Assignments: daily-scoped roster for shift-based operators

CREATE TABLE facility_operator_assignments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    operator_id     UUID NOT NULL REFERENCES facility_operators(id),
    facility_id     UUID NOT NULL,
    assigned_date   DATE NOT NULL,
    shift_start     TIMESTAMPTZ NOT NULL,
    shift_end       TIMESTAMPTZ NOT NULL,
    assigned_by     UUID NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_assignment_operator_date ON facility_operator_assignments(operator_id, assigned_date, status);
CREATE INDEX idx_assignment_facility_date ON facility_operator_assignments(facility_id, assigned_date, status);
