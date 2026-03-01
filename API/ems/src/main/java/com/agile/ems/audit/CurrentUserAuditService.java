package com.agile.ems.audit;

import com.agile.ems.user.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserAuditService {

    private static final Long DEFAULT_ADMIN_USER_ID = 1L;
    private final UserRepository userRepository;

    public Long getCurrentUserIdOrDefault() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return DEFAULT_ADMIN_USER_ID;
        }

        String identifier = extractIdentifier(authentication);
        if (identifier == null || identifier.trim().isEmpty()) {
            return DEFAULT_ADMIN_USER_ID;
        }

        return userRepository.findByEmailOrEmpId(identifier, identifier)
                .map(user -> user.getId())
                .orElse(DEFAULT_ADMIN_USER_ID);
    }

    private String extractIdentifier(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            return userDetails.getUsername();
        }

        if (principal instanceof String) {
            String principalString = (String) principal;
            if (!"anonymousUser".equalsIgnoreCase(principalString)) {
                return principalString;
            }
        }

        String name = authentication.getName();
        if (name != null && !"anonymousUser".equalsIgnoreCase(name)) {
            return name;
        }

        return null;
    }
}
