package com.myApp.ExpenseTracker.Config;

import com.myApp.ExpenseTracker.Model.CustomUserDetails;
import com.myApp.ExpenseTracker.Service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class KeyResolver {
    private final JwtService jwtService;

    public KeyResolver(JwtService jwtService) {
        this.jwtService = jwtService;
    }
    public String resolveKey(HttpServletRequest request) {

        // 1. Try SecurityContext (fast path for authenticated users)
        String userKey = resolveFromSecurityContext();
        if (userKey != null) {
            return userKey;
        }

        // 2. Try Access Token manually (in case SecurityContext not set)
        userKey = resolveFromAccessToken(request);
        if (userKey != null) {
            return userKey;
        }

        // 3. Try Refresh Token
        userKey = resolveFromRefreshToken(request);
        if (userKey != null) {
            return userKey;
        }

        // 4. FINAL FALLBACK → IP
        return "ip:" + extractClientIp(request);
    }

    // ------------------ SECURITY CONTEXT ------------------

    private String resolveFromSecurityContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && auth.getPrincipal() instanceof CustomUserDetails userDetails) {

            return "user:" + userDetails.getId();
        }
        return null;
    }
    // ------------------ ACCESS TOKEN ------------------
    private String resolveFromAccessToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        try {
            Long userId = jwtService.extractUserid(token);
            if (userId != null) {
                return "user:" + userId;
            }
        } catch (Exception ignored) {
            // invalid / expired → fallback
        }
        return null;
    }
    // ------------------ REFRESH TOKEN ------------------
    private String resolveFromRefreshToken(HttpServletRequest request) {
        String refreshToken = request.getHeader("Refresh-Token");
        if (refreshToken == null || refreshToken.isEmpty()) {
            return null;
        }
        try {
            Long userId = jwtService.extractUserid(refreshToken);
            if (userId != null) {
                return "user:" + userId;
            }
        } catch (Exception ignored) {
            // fallback
        }
        return null;
    }

    // ------------------ IP EXTRACTION ------------------
    private String extractClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }
        return request.getRemoteAddr();
    }
}