package com.bhagwat.scm.carrierService.repository;

import com.bhagwat.scm.carrierService.entity.FacilityOperatorAssignment;
import com.bhagwat.scm.carrierService.entity.FacilityOperatorAssignment.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FacilityOperatorAssignmentRepository extends JpaRepository<FacilityOperatorAssignment, UUID> {

    /**
     * Find active assignment for an operator at a specific facility where current time
     * falls within the shift window.
     */
    @Query("""
        SELECT a FROM FacilityOperatorAssignment a
        WHERE a.operatorId = :operatorId
          AND a.facilityId = :facilityId
          AND a.status = 'ACTIVE'
          AND a.shiftStart <= :currentTime
          AND a.shiftEnd >= :currentTime
        """)
    Optional<FacilityOperatorAssignment> findActiveAssignment(
            @Param("operatorId") UUID operatorId,
            @Param("facilityId") UUID facilityId,
            @Param("currentTime") Instant currentTime);

    /**
     * Check for overlapping assignments: same operator, different facility, overlapping time window.
     */
    @Query("""
        SELECT a FROM FacilityOperatorAssignment a
        WHERE a.operatorId = :operatorId
          AND a.facilityId != :facilityId
          AND a.status = 'ACTIVE'
          AND a.shiftStart < :shiftEnd
          AND a.shiftEnd > :shiftStart
        """)
    List<FacilityOperatorAssignment> findOverlappingAssignments(
            @Param("operatorId") UUID operatorId,
            @Param("facilityId") UUID facilityId,
            @Param("shiftStart") Instant shiftStart,
            @Param("shiftEnd") Instant shiftEnd);

    /**
     * Find all active assignments that have expired (shift_end + grace period passed).
     */
    @Query("""
        SELECT a FROM FacilityOperatorAssignment a
        WHERE a.status = 'ACTIVE'
          AND a.shiftEnd < :graceExpiry
        """)
    List<FacilityOperatorAssignment> findExpiredActiveAssignments(@Param("graceExpiry") Instant graceExpiry);

    /**
     * Count assignments for an operator on a given date (max 3 per day).
     */
    long countByOperatorIdAndAssignedDate(UUID operatorId, LocalDate assignedDate);
}
