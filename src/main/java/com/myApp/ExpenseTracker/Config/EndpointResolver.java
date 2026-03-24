package com.myApp.ExpenseTracker.Config;

import com.myApp.ExpenseTracker.Utils.KeyType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.List;

@Component
public class EndpointResolver {
    private final RateLimitProperties properties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public EndpointResolver(RateLimitProperties properties) {
        this.properties = properties;
    }

    public EndpointConfig resolve(HttpServletRequest request) {
        String path = request.getRequestURI();

        if (properties.getEndpoints() == null) {
            throw new IllegalStateException("Rate limit endpoints not configured");
        }

        for (EndpointConfig config : properties.getEndpoints()) {
            for (String pattern : config.getCompiledPatterns()) {
                if (pathMatcher.match(pattern, path)) {
                    return config;
                }
            }
        }

        // 🔥 Fallback (very important)
        return getDefaultConfig();
    }

    private EndpointConfig getDefaultConfig() {
        // last one is default (API)
        List<EndpointConfig> endpoints = properties.getEndpoints();
        return endpoints.get(endpoints.size() - 1);
    }
}