package com.agile.ems.auth.security;

import com.agile.ems.utils.ApiResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/departments/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST,   "/api/users").hasAnyRole("ADMIN", "HR")
                .requestMatchers(HttpMethod.PUT,    "/api/users/**").hasAnyRole("ADMIN", "HR")
                .requestMatchers(HttpMethod.GET,    "/api/users").hasAnyRole("ADMIN", "HR")
                .requestMatchers(HttpMethod.GET,    "/api/users/generate-emp-id").hasAnyRole("ADMIN", "HR")
                .requestMatchers(HttpMethod.GET,    "/api/users/check-emp-id").hasAnyRole("ADMIN", "HR")
                .requestMatchers(HttpMethod.GET,    "/api/users/check-email").hasAnyRole("ADMIN", "HR")

                // Department management (reads are open to all — see section 4)
                .requestMatchers(HttpMethod.POST,   "/api/departments").hasAnyRole("ADMIN", "HR")
                .requestMatchers(HttpMethod.PUT,    "/api/departments/**").hasAnyRole("ADMIN", "HR")

                // Feedback session administration
                .requestMatchers(HttpMethod.POST,   "/api/feedback/sessions").hasAnyRole("ADMIN", "HR")
                .requestMatchers(HttpMethod.POST,   "/api/feedback/sessions/*/assign").hasAnyRole("ADMIN", "HR")
                .requestMatchers(HttpMethod.GET,    "/api/feedback/sessions/*/scores").hasAnyRole("ADMIN", "HR")

                // Manager review submission (HR/ADMIN act as managers)
                .requestMatchers(HttpMethod.POST,   "/api/feedback/sessions/*/manager-review").hasAnyRole("ADMIN", "HR")

                // ── 4. ALL AUTHENTICATED USERS ───────────────────────────────
                // Own profile (any employee can view/update themselves)
                .requestMatchers(HttpMethod.GET,    "/api/users/*").authenticated()
                .requestMatchers(HttpMethod.PATCH,  "/api/users/*/password").authenticated()
                .requestMatchers(HttpMethod.POST,   "/api/users/*/details").authenticated()

                // Departments — readable by everyone (needed for dropdowns)
                .requestMatchers(HttpMethod.GET,    "/api/departments/**").authenticated()

                // Dashboard
                .requestMatchers("/api/dashboard/**").authenticated()

                // Goals — each user manages their own
                .requestMatchers("/api/goals/**").authenticated()

                // Feedback — list sessions and templates (read-only for employees)
                .requestMatchers(HttpMethod.GET,    "/api/feedback/sessions").authenticated()
                .requestMatchers("/api/feedback/templates/**").authenticated()

                // Review submission (self + peer open to all employees)
                .requestMatchers(HttpMethod.POST,   "/api/feedback/sessions/*/self-review").authenticated()
                .requestMatchers(HttpMethod.POST,   "/api/feedback/sessions/*/peer-review").authenticated()
                .requestMatchers(HttpMethod.GET,    "/api/feedback/sessions/my-reviews").authenticated()

                // ── 5. FALLBACK — deny everything else ───────────────────────
                .anyRequest().denyAll()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) ->
                    writeError(res, HttpStatus.UNAUTHORIZED.value(), "Unauthorized"))
                .accessDeniedHandler((req, res, e) ->
                    writeError(res, HttpStatus.FORBIDDEN.value(), "Access denied"))
            );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // strength 10 (default)
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        ApiResponseDto<Object> body = ApiResponseDto.failure(status, message, null);
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
