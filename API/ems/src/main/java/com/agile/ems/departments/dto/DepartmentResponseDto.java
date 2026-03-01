package com.agile.ems.departments.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DepartmentResponseDto {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
}
