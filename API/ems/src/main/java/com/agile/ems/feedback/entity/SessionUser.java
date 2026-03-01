package com.agile.ems.feedback.entity;

import com.agile.ems.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "session_user")
@Getter
@Setter
public class SessionUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private FeedbackSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    /** The specific peer who is pre-assigned to review this employee. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "peer_reviewer_id")
    private User peerReviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "self_template_id", nullable = false)
    private FeedbackTemplate selfTemplate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "peer_template_id", nullable = false)
    private FeedbackTemplate peerTemplate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_template_id", nullable = false)
    private FeedbackTemplate managerTemplate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
