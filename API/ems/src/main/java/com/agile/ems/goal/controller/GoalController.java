package com.agile.ems.goal.controller;

import com.agile.ems.goal.dto.AssignableUserDto;
import com.agile.ems.goal.dto.GoalRequestDto;
import com.agile.ems.goal.dto.GoalResponseDto;
import com.agile.ems.goal.service.GoalService;
import com.agile.ems.utils.ApiResponseDto;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<GoalResponseDto>> create(@Valid @RequestBody GoalRequestDto requestDto) {
        GoalResponseDto data = goalService.create(requestDto);
        ApiResponseDto<GoalResponseDto> response = ApiResponseDto.success(
                HttpStatus.CREATED.value(),
                "Goal created successfully",
                data
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<GoalResponseDto>> getById(@PathVariable Long id) {
        GoalResponseDto data = goalService.getById(id);
        ApiResponseDto<GoalResponseDto> response = ApiResponseDto.success(
                HttpStatus.OK.value(),
                "Goal fetched successfully",
                data
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<GoalResponseDto>>> getAllForCurrentUser() {
        List<GoalResponseDto> data = goalService.getAllForCurrentUser();
        ApiResponseDto<List<GoalResponseDto>> response = ApiResponseDto.success(
                HttpStatus.OK.value(),
                "Goals fetched successfully",
                data
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<GoalResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody GoalRequestDto requestDto
    ) {
        GoalResponseDto data = goalService.update(id, requestDto);
        ApiResponseDto<GoalResponseDto> response = ApiResponseDto.success(
                HttpStatus.OK.value(),
                "Goal updated successfully",
                data
        );
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponseDto<GoalResponseDto>> markCompleted(@PathVariable Long id) {
        GoalResponseDto data = goalService.markCompleted(id);
        ApiResponseDto<GoalResponseDto> response = ApiResponseDto.success(
                HttpStatus.OK.value(),
                "Goal marked as completed",
                data
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Object>> delete(@PathVariable Long id) {
        goalService.delete(id);
        ApiResponseDto<Object> response = ApiResponseDto.success(
                HttpStatus.OK.value(),
                "Goal deleted successfully",
                null
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/assignable-users")
    public ResponseEntity<ApiResponseDto<List<AssignableUserDto>>> getAssignableUsers() {
        List<AssignableUserDto> data = goalService.getAssignableUsers();
        ApiResponseDto<List<AssignableUserDto>> response = ApiResponseDto.success(
                HttpStatus.OK.value(),
                "Assignable users fetched successfully",
                data
        );
        return ResponseEntity.ok(response);
    }
}
