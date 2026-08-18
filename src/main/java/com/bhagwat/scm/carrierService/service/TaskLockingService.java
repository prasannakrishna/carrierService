package com.bhagwat.scm.carrierService.service;

import com.bhagwat.scm.carrierService.entity.DomainOperatorActivityLog;
import com.bhagwat.scm.carrierService.entity.TaskHistory;
import com.bhagwat.scm.carrierService.repository.DomainOperatorActivityLogRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Task Locking Service — implements exclusive operator assignment to tasks.
 * 
 * Atomic claim via optimistic locking (WHERE locked_to_operator_id IS NULL).
 * Every task event carries performed_by attribution.
 * Grace period on session expiry before auto-release.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskLockingService {

    private final JdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;
    private final DomainOperatorActivityLogRepository activityLogRepository;

    private static final int GRACE_PERIOD_MINUTES = 10;

    /**
     * Claim a task — atomic set of locked_to_operator_id.
     * Fails with TASK_LOCKED if already locked to a different operator.
     */
    @Transactional
    public ClaimResult claimTask(UUID taskId, UUID operatorId, UUID facilityId, UUID sessionId) {
        // Atomic claim: only succeeds if task is unlocked or already locked to this operator
        int updated = jdbcTemplate.update("""
            UPDATE carrier_tasks 
            SET locked_to_operator_id = ?, locked_at = now(), lock_grace_expires_at = NULL
            WHERE id = ? AND (locked_to_operator_id IS NULL OR locked_to_operator_id = ?)
            """, operatorId, taskId, operatorId);

        if (updated == 0) {
            // Task is locked to a different operator
            var lockInfo = jdbcTemplate.queryForMap(
                    "SELECT locked_to_operator_id, locked_at FROM carrier_tasks WHERE id = ?", taskId);
            return ClaimResult.locked(
                    (UUID) lockInfo.get("locked_to_operator_id"),
                    ((java.sql.Timestamp) lockInfo.get("locked_at")).toInstant()
            );
        }

        // Record in task history
        persistTaskHistory(taskId, operatorId, "CLAIMED", null);

        // Write activity log
        writeActivityLog(operatorId, facilityId, "task-claim", "task", taskId,
                Map.of("action", "claim"), "SUCCESS", sessionId);

        return ClaimResult.claimed();
    }

    /**
     * Complete a task — clears lock, records completion.
     */
    @Transactional
    public void completeTask(UUID taskId, UUID operatorId, UUID facilityId, UUID sessionId) {
        int updated = jdbcTemplate.update("""
            UPDATE carrier_tasks 
            SET locked_to_operator_id = NULL, locked_at = NULL, lock_grace_expires_at = NULL, status = 'COMPLETED'
            WHERE id = ? AND locked_to_operator_id = ?
            """, taskId, operatorId);

        if (updated == 0) {
            throw new TaskLockException("Cannot complete task — not locked to this operator");
        }

        persistTaskHistory(taskId, operatorId, "COMPLETED", null);
        writeActivityLog(operatorId, facilityId, "task-complete", "task", taskId,
                Map.of("action", "complete"), "SUCCESS", sessionId);
    }

    /**
     * Release a task — operator voluntarily releases lock.
     */
    @Transactional
    public void releaseTask(UUID taskId, UUID operatorId, UUID facilityId, UUID sessionId) {
        int updated = jdbcTemplate.update("""
            UPDATE carrier_tasks 
            SET locked_to_operator_id = NULL, locked_at = NULL, lock_grace_expires_at = NULL, status = 'AVAILABLE'
            WHERE id = ? AND locked_to_operator_id = ?
            """, taskId, operatorId);

        if (updated == 0) {
            throw new TaskLockException("Cannot release task — not locked to this operator");
        }

        persistTaskHistory(taskId, operatorId, "RELEASED", null);
        writeActivityLog(operatorId, facilityId, "task-release", "task", taskId,
                Map.of("action", "release"), "SUCCESS", sessionId);
    }

    /**
     * Supervisor force-release — admin overrides an existing lock.
     */
    @Transactional
    public void forceRelease(UUID taskId, UUID supervisorId, UUID facilityId, UUID sessionId) {
        var taskRow = jdbcTemplate.queryForMap(
                "SELECT locked_to_operator_id FROM carrier_tasks WHERE id = ?", taskId);
        UUID previousOperator = (UUID) taskRow.get("locked_to_operator_id");

        jdbcTemplate.update("""
            UPDATE carrier_tasks 
            SET locked_to_operator_id = NULL, locked_at = NULL, lock_grace_expires_at = NULL, status = 'AVAILABLE'
            WHERE id = ?
            """, taskId);

        persistTaskHistory(taskId, supervisorId, "FORCE_RELEASED", previousOperator);
        writeActivityLog(supervisorId, facilityId, "task-force-release", "task", taskId,
                Map.of("action", "force_release", "previous_operator", previousOperator.toString()), "SUCCESS", sessionId);
    }

    /**
     * Set grace period on session expiry — background job will auto-release after grace period.
     */
    public void setGracePeriod(UUID operatorId) {
        Instant graceExpiry = Instant.now().plusSeconds(GRACE_PERIOD_MINUTES * 60L);
        jdbcTemplate.update("""
            UPDATE carrier_tasks SET lock_grace_expires_at = ? 
            WHERE locked_to_operator_id = ? AND lock_grace_expires_at IS NULL
            """, java.sql.Timestamp.from(graceExpiry), operatorId);
    }

    /**
     * Auto-release tasks with expired grace periods (called by background job).
     */
    @Transactional
    public int autoReleaseExpiredGracePeriods() {
        var expiredTasks = jdbcTemplate.queryForList("""
            SELECT id, locked_to_operator_id FROM carrier_tasks 
            WHERE lock_grace_expires_at IS NOT NULL AND lock_grace_expires_at < now()
            """);

        for (var task : expiredTasks) {
            UUID taskId = (UUID) task.get("id");
            UUID operatorId = (UUID) task.get("locked_to_operator_id");

            jdbcTemplate.update("""
                UPDATE carrier_tasks 
                SET locked_to_operator_id = NULL, locked_at = NULL, lock_grace_expires_at = NULL, status = 'AVAILABLE'
                WHERE id = ?
                """, taskId);

            persistTaskHistory(taskId, operatorId, "TIMEOUT_RELEASED", null);
        }

        return expiredTasks.size();
    }

    private void persistTaskHistory(UUID taskId, UUID operatorId, String action, UUID previousOperatorId) {
        jdbcTemplate.update("""
            INSERT INTO task_history (id, task_id, operator_id, action, previous_lock_operator_id, performed_at)
            VALUES (gen_random_uuid(), ?, ?, ?, ?, now())
            """, taskId, operatorId, action, previousOperatorId);
    }

    private void writeActivityLog(UUID operatorId, UUID facilityId, String actionType, String entityType,
                                   UUID entityId, Map<String, Object> metadata, String outcome, UUID sessionId) {
        try {
            activityLogRepository.save(DomainOperatorActivityLog.builder()
                    .operatorId(operatorId)
                    .facilityId(facilityId)
                    .actionType(actionType)
                    .entityType(entityType)
                    .entityId(entityId)
                    .actionMetadata(metadata)
                    .outcome(outcome)
                    .sessionId(sessionId)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to write activity log for task {}: {}", entityId, e.getMessage());
            // Don't block primary operation on activity log failure (R14.8)
        }
    }

    // Result types
    public record ClaimResult(boolean success, UUID lockedTo, Instant lockedAt) {
        public static ClaimResult claimed() { return new ClaimResult(true, null, null); }
        public static ClaimResult locked(UUID lockedTo, Instant lockedAt) { return new ClaimResult(false, lockedTo, lockedAt); }
    }

    public static class TaskLockException extends RuntimeException {
        public TaskLockException(String msg) { super(msg); }
    }
}
