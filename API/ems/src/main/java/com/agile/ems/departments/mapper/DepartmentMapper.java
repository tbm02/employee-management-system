package com.agile.ems.departments.mapper;

import com.agile.ems.departments.Department;
import com.agile.ems.departments.dto.DepartmentRequestDto;
import com.agile.ems.departments.dto.DepartmentResponseDto;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {

    public Department toEntity(DepartmentRequestDto requestDto) {
        Department department = new Department();
        department.setName(requestDto.getName());
        department.setDescription(requestDto.getDescription());
        return department;
    }

    public DepartmentResponseDto toResponse(Department department) {
        return DepartmentResponseDto.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .createdAt(department.getCreatedAt())
                .build();
    }
}
