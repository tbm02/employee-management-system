package com.agile.ems.audit;

import com.agile.ems.auth.security.AuthUserPrincipal;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserAuditService {

    private static final Long DEFAULT_ADMIN_USER_ID = 1L;

    public Long getCurrentUserIdOrDefault() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return DEFAULT_ADMIN_USER_ID;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthUserPrincipal authUserPrincipal) {
            Long userId = authUserPrincipal.getUserId();
            return userId != null ? userId : DEFAULT_ADMIN_USER_ID;
        }

        return DEFAULT_ADMIN_USER_ID;
    }
}
