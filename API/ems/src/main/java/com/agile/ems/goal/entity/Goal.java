package com.agile.ems.goal.entity;

import com.agile.ems.audit.EntityAuditInfo;
import com.agile.ems.goal.enums.GoalStatus;
import com.agile.ems.goal.enums.Quarter;
import com.agile.ems.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "goals")
@Getter
@Setter
public class Goal extends EntityAuditInfo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =============================
    // Employee who owns goal
    // =============================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User employee;

    private String description;

    @Enumerated(EnumType.STRING)
    private Quarter quarter;

    private Integer year;

    @Enumerated(EnumType.STRING)
    private GoalStatus status;

    // =============================
    // Manager who approved
    // =============================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    private Boolean isCompleted = false;

//    @PrePersist
//    public void onCreate() {
//        if (status == null) {
//            status = GoalStatus.DRAFT;
//        }
//    }
}
