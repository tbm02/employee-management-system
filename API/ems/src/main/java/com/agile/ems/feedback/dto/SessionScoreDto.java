package com.agile.ems.feedback.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Per-employee score breakdown for a feedback session.
 * Weights: self=0.30, manager=0.50, peer=0.20 (normalised to 1 when components are missing).
 */
@Getter
@Builder
public class SessionScoreDto {

    private Long   employeeId;
    private String empId;
    private String firstName;
    private String lastName;

    private Long   departmentId;
    private String departmentName;

    // ── Raw averages (1–5 scale) ──────────────────────────────────────────
    private Double selfAvg;
    private Double peerAvg;
    private Double managerAvg;

    // ── Normalised (÷5 → 0–1) ────────────────────────────────────────────
    private Double selfNorm;
    private Double peerNorm;
    private Double managerNorm;

    // ── Weighted composite: 0.30·self + 0.50·manager + 0.20·peer ─────────
    private Double weightedScore;
}
