package com.agile.ems.user.service;

import com.agile.ems.user.dto.ChangePasswordRequestDto;
import com.agile.ems.user.dto.PersonalDetailsRequestDto;
import com.agile.ems.user.dto.UserRequestDto;
import com.agile.ems.user.dto.UserResponseDto;
import java.util.List;

public interface UserService {
    UserResponseDto create(UserRequestDto requestDto);

    UserResponseDto getById(Long id);

    List<UserResponseDto> getAll();

    UserResponseDto update(Long id, UserRequestDto requestDto);

    void changePassword(Long id, ChangePasswordRequestDto requestDto);

    void savePersonalDetails(Long id, PersonalDetailsRequestDto requestDto);

    void delete(Long id);

    String generateEmpId();

    boolean isEmpIdTaken(String empId);

    boolean isEmailTaken(String email);

    void validateManagerOfEmployee(Long managerId, Long employeeId);
}
