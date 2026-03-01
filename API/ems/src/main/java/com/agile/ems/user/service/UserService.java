package com.agile.ems.user.service;

import com.agile.ems.user.dto.UserRequestDto;
import com.agile.ems.user.dto.UserResponseDto;
import java.util.List;

public interface UserService {
    UserResponseDto create(UserRequestDto requestDto);

    UserResponseDto getById(Long id);

    List<UserResponseDto> getAll();

    UserResponseDto update(Long id, UserRequestDto requestDto);

    void delete(Long id);
}
