package com.agile.ems.feedback.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Per-employee score breakdown for a feedback session.
 *
 * Individual averages are on the 1–5 scale.
 * Normalised values divide by 5 to express as 0–1.
 * Weighted score = 0.30 × selfNorm + 0.50 × managerNorm + 0.20 × peerNorm
 */
@Getter
@Builder
public class SessionScoreDto {

    private Long   employeeId;
    private String empId;
    private String firstName;
    private String lastName;
    private String departmentName;

    // ── Raw averages (1–5 scale, null if no responses) ───────
    private Double selfAvg;
    private Double peerAvg;
    private Double managerAvg;

    // ── Normalised (0–1 scale) ────────────────────────────────
    private Double selfNorm;
    private Double peerNorm;
    private Double managerNorm;

    /**
     * Weighted composite: 0.30 self + 0.50 manager + 0.20 peer.
     * Only components with actual responses are included and
     * the weights are re-normalised accordingly.
     */
    private Double weightedScore;
}
