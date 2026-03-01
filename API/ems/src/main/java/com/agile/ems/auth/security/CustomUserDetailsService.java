package com.agile.ems.auth.security;

import com.agile.ems.user.Repository.UserRepository;
import com.agile.ems.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmailOrEmpId(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .disabled(!Boolean.TRUE.equals(user.getEnabled()))
                .build();
    }

    public User loadDomainUserByIdentifier(String identifier) {
        return userRepository.findByEmailOrEmpId(identifier, identifier)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
