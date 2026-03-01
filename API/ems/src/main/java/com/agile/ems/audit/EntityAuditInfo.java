package com.agile.ems.audit;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class EntityAuditInfo {

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private Long createdBy;

    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt;

    @Column(name = "modified_by", nullable = false)
    private Long modifiedBy;

    @PrePersist
    protected void onPrePersistAudit() {
        LocalDateTime now = LocalDateTime.now();
        Long actorId = resolveActorId();

        if (createdAt == null) {
            createdAt = now;
        }
        if (createdBy == null) {
            createdBy = actorId;
        }

        modifiedAt = now;
        modifiedBy = actorId;
    }

    @PreUpdate
    protected void onPreUpdateAudit() {
        modifiedAt = LocalDateTime.now();
        modifiedBy = resolveActorId();
    }

    private Long resolveActorId() {
        CurrentUserAuditService auditService = SpringContextHolder.getBean(CurrentUserAuditService.class);
        if (auditService == null) {
            return 1L;
        }
        return auditService.getCurrentUserIdOrDefault();
    }
}
