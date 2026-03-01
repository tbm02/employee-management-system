package com.agile.ems.feedback.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Request body for POST /api/feedback/sessions/{sessionId}/assign.
 *
 * Templates are shared across all employees in this batch.
 * Each employee entry individually names who will peer-review them.
 */
@Getter
@Setter
public class AssignSessionEmployeesRequest {

    /** Per-employee pairing: who is being reviewed + who will peer-review them. */
    @NotEmpty
    @Valid
    private List<EmployeeAssignment> assignments;

    @NotNull
    private Long selfTemplateId;

    @NotNull
    private Long peerTemplateId;

    @NotNull
    private Long managerTemplateId;

    // ── Nested DTO ─────────────────────────────────────────
    @Getter
    @Setter
    public static class EmployeeAssignment {
        @NotNull
        private Long employeeId;

        /** The colleague who will write the peer review for this employee. */
        @NotNull
        private Long peerReviewerId;
    }
}
