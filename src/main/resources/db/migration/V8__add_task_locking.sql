-- Task Locking: exclusive operator assignment to tasks
-- Applied to domain-specific task tables (wms_tasks, store_tasks, carrier_tasks)

-- For carrierService, this applies to trip-related tasks. Referenced by
-- TaskLockingService but never actually created by any prior migration —
-- this has been silently broken (and, since Flyway rolled back the failed
-- transaction without recording it, blocking every fresh startup) since
-- this migration was written.
CREATE TABLE IF NOT EXISTS carrier_tasks (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    status                      VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    locked_to_operator_id       UUID,
    locked_at                   TIMESTAMPTZ,
    lock_grace_expires_at       TIMESTAMPTZ,
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE carrier_tasks ADD COLUMN IF NOT EXISTS locked_to_operator_id UUID;
ALTER TABLE carrier_tasks ADD COLUMN IF NOT EXISTS locked_at TIMESTAMPTZ;
ALTER TABLE carrier_tasks ADD COLUMN IF NOT EXISTS lock_grace_expires_at TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_carrier_tasks_lock ON carrier_tasks(locked_to_operator_id) WHERE locked_to_operator_id IS NOT NULL;

-- Task history for audit trail
CREATE TABLE IF NOT EXISTS task_history (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id                     UUID NOT NULL,
    operator_id                 UUID NOT NULL,
    action                      VARCHAR(30) NOT NULL,
    previous_lock_operator_id   UUID,
    performed_at                TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_task_history_task ON task_history(task_id, performed_at DESC);
CREATE INDEX idx_task_history_operator ON task_history(operator_id, performed_at DESC);
