package com.agile.ems.goal.dto;

import com.agile.ems.goal.enums.GoalStatus;
import com.agile.ems.goal.enums.Quarter;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GoalResponseDto {
    private Long id;
    private String description;
    private Quarter quarter;
    private Integer year;
    private GoalStatus status;
    private Boolean isCompleted;

    private Long employeeId;
    private String employeeName;
    private String employeeEmail;

    private Long createdBy;
    private Boolean createdByMe;

    private Boolean canEdit;
    private Boolean canDelete;
    private Boolean canMarkCompleted;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
