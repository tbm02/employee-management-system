package com.agile.ems.user.mapper;

import com.agile.ems.user.entity.User;
import com.agile.ems.user.dto.UserRequestDto;
import com.agile.ems.user.dto.UserResponseDto;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(UserRequestDto requestDto) {
        User user = new User();
        user.setEmpId(requestDto.getEmpId());
        user.setFirstName(requestDto.getFirstName());
        user.setLastName(requestDto.getLastName());
        user.setEmail(requestDto.getEmail());
        user.setPassword(requestDto.getPassword());
        user.setRole(requestDto.getRole());
        user.setDepartmentId(requestDto.getDepartmentId());
        user.setEnabled(requestDto.getEnabled());
        user.setFirstLogin(true);
        user.setPasswordUpdated(false);
        return user;
    }

    public UserResponseDto toResponse(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .empId(user.getEmpId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .departmentId(user.getDepartmentId())
                .enabled(user.getEnabled())
                .firstLogin(user.getFirstLogin())
                .passwordUpdated(user.getPasswordUpdated())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
