package com.agile.ems.goal.service;

import com.agile.ems.goal.dto.AssignableUserDto;
import com.agile.ems.goal.dto.GoalRequestDto;
import com.agile.ems.goal.dto.GoalResponseDto;
import java.util.List;

public interface GoalService {

    GoalResponseDto create(GoalRequestDto requestDto);

    GoalResponseDto update(Long id, GoalRequestDto requestDto);

    GoalResponseDto getById(Long id);

    List<GoalResponseDto> getAllForCurrentUser();

    GoalResponseDto markCompleted(Long id);

    void delete(Long id);

    List<AssignableUserDto> getAssignableUsers();
}
