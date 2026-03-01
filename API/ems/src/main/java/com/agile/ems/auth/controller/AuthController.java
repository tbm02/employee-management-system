package com.agile.ems.auth.controller;

import com.agile.ems.auth.dto.LoginRequestDto;
import com.agile.ems.auth.dto.LoginResponseDto;
import com.agile.ems.auth.service.AuthService;
import com.agile.ems.utils.ApiResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto requestDto) {
        LoginResponseDto data = authService.login(requestDto);
        ApiResponseDto<LoginResponseDto> response = ApiResponseDto.success(
                HttpStatus.OK.value(),
                "Login successful",
                data
        );
        return ResponseEntity.ok(response);
    }
}
