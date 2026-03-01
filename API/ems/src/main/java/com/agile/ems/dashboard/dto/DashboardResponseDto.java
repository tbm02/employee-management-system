package com.agile.ems.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardResponseDto {
    private Integer totalEmployees;
    private Integer totalDepartments;
    private Integer pendingRequests;
    private String note;
}
