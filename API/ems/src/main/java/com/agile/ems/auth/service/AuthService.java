package com.agile.ems.auth.service;

import com.agile.ems.auth.dto.LoginRequestDto;
import com.agile.ems.auth.dto.LoginResponseDto;
import com.agile.ems.auth.security.CustomUserDetailsService;
import com.agile.ems.user.entity.User;
import com.agile.ems.utils.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;

    public LoginResponseDto login(LoginRequestDto requestDto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestDto.getIdentifier(), requestDto.getPassword())
            );
        } catch (AuthenticationException ex) {
            throw new BadRequestException("Invalid credentials");
        }

        User user;
        try {
            user = customUserDetailsService.loadDomainUserByIdentifier(requestDto.getIdentifier());
        } catch (UsernameNotFoundException ex) {
            throw new BadRequestException("Invalid credentials");
        }

        String accessToken = jwtService.generateToken(user);

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresInMs(jwtService.getJwtExpirationMs())
                .userId(user.getId())
                .email(user.getEmail())
                .empId(user.getEmpId())
                .role(user.getRole())
                .firstLogin(user.getFirstLogin())
                .passwordUpdated(user.getPasswordUpdated())
                .personalDetailsUpdated(user.getUserDetails() != null)
                .build();
    }
}
