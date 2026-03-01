package com.agile.ems.user.dto;

import com.agile.ems.user.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private Role role;
    private Long departmentId;
    private Boolean enabled;
    @JsonProperty("first_login")
    private Boolean firstLogin;
    @JsonProperty("is_password_updated")
    private Boolean passwordUpdated;
    private LocalDateTime createdAt;
}
