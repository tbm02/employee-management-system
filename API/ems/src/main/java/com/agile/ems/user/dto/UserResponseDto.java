package com.agile.ems.user.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponseDto {
    private Long id;
    private String empId;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private Long departmentId;
    private Boolean enabled;
    private LocalDateTime createdAt;
}
