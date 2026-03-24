package com.myApp.ExpenseTracker.Config;

import com.myApp.ExpenseTracker.Model.CustomUserDetails;
import com.myApp.ExpenseTracker.Service.CurrentUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class ProCurrentUserProvider implements CurrentUserProvider {
    @Override
    public Long getCurrentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();//!authentication.isAuthenticated() extra
        if (authentication == null || !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {

            throw new AccessDeniedException("User not authenticated");
        }
        return userDetails.getId();
    }
}
