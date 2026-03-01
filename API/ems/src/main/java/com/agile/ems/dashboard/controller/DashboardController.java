package com.agile.ems.dashboard.controller;

import com.agile.ems.dashboard.dto.DashboardResponseDto;
import com.agile.ems.utils.ApiResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @GetMapping
    public ResponseEntity<ApiResponseDto<DashboardResponseDto>> getDashboard() {
        DashboardResponseDto data = DashboardResponseDto.builder()
                .totalEmployees(24)
                .totalDepartments(6)
                .pendingRequests(3)
                .note("Dummy dashboard data for assignment")
                .build();

        ApiResponseDto<DashboardResponseDto> response = ApiResponseDto.success(
                HttpStatus.OK.value(),
                "Dashboard fetched successfully",
                data
        );

        return ResponseEntity.ok(response);
    }
}
