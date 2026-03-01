package com.agile.ems.auth.dto;

import com.agile.ems.user.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponseDto {
    private String accessToken;
    private String tokenType;
    private Long expiresInMs;
    private Long userId;
    private String email;
    private String empId;
    private Role role;
    @JsonProperty("first_login")
    private Boolean firstLogin;
    @JsonProperty("is_password_updated")
    private Boolean passwordUpdated;
    @JsonProperty("is_personal_details_updated")
    private Boolean personalDetailsUpdated;
}
