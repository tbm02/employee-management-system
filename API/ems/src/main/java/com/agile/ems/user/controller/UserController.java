package com.agile.ems.user.controller;

import com.agile.ems.user.dto.ChangePasswordRequestDto;
import com.agile.ems.user.dto.PersonalDetailsRequestDto;
import com.agile.ems.user.dto.UserRequestDto;
import com.agile.ems.user.dto.UserResponseDto;
import com.agile.ems.user.service.UserService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<UserResponseDto>> create(@Valid @RequestBody UserRequestDto requestDto) {
        UserResponseDto data = userService.create(requestDto);
        ApiResponseDto<UserResponseDto> response = ApiResponseDto.success(
                HttpStatus.CREATED.value(),
                "User created successfully",
                data
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> getById(@PathVariable Long id) {
        UserResponseDto data = userService.getById(id);
        ApiResponseDto<UserResponseDto> response = ApiResponseDto.success(
                HttpStatus.OK.value(),
                "User fetched successfully",
                data
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<UserResponseDto>>> getAll() {
        List<UserResponseDto> data = userService.getAll();
        ApiResponseDto<List<UserResponseDto>> response = ApiResponseDto.success(
                HttpStatus.OK.value(),
                "Users fetched successfully",
                data
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDto requestDto
    ) {
        UserResponseDto data = userService.update(id, requestDto);
        ApiResponseDto<UserResponseDto> response = ApiResponseDto.success(
                HttpStatus.OK.value(),
                "User updated successfully",
                data
        );
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<ApiResponseDto<Object>> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequestDto requestDto
    ) {
        userService.changePassword(id, requestDto);
        ApiResponseDto<Object> response = ApiResponseDto.success(
                HttpStatus.OK.value(),
                "Password updated successfully",
                null
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/details")
    public ResponseEntity<ApiResponseDto<Object>> savePersonalDetails(
            @PathVariable Long id,
            @RequestBody PersonalDetailsRequestDto requestDto
    ) {
        userService.savePersonalDetails(id, requestDto);
        ApiResponseDto<Object> response = ApiResponseDto.success(
                HttpStatus.OK.value(),
                "Personal details saved successfully",
                null
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Object>> delete(@PathVariable Long id) {
        userService.delete(id);
        ApiResponseDto<Object> response = ApiResponseDto.success(
                HttpStatus.OK.value(),
                "User deleted successfully",
                null
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/generate-emp-id")
    public ResponseEntity<ApiResponseDto<String>> generateEmpId() {
        String empId = userService.generateEmpId();
        ApiResponseDto<String> response = ApiResponseDto.success(
                HttpStatus.OK.value(),
                "Employee ID generated",
                empId
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-emp-id")
    public ResponseEntity<ApiResponseDto<Boolean>> checkEmpId(@RequestParam String empId) {
        boolean taken = userService.isEmpIdTaken(empId);
        ApiResponseDto<Boolean> response = ApiResponseDto.success(
                HttpStatus.OK.value(),
                taken ? "Employee ID is already taken" : "Employee ID is available",
                taken
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-email")
    public ResponseEntity<ApiResponseDto<Boolean>> checkEmail(@RequestParam String email) {
        boolean taken = userService.isEmailTaken(email);
        ApiResponseDto<Boolean> response = ApiResponseDto.success(
                HttpStatus.OK.value(),
                taken ? "Email is already taken" : "Email is available",
                taken
        );
        return ResponseEntity.ok(response);
    }
}
