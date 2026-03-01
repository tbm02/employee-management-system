package com.agile.ems.goal.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssignableUserDto {
    private Long id;
    private String empId;
    private String fullName;
    private String email;
}
