package com.myApp.ExpenseTracker.Ratelimit;

import com.myApp.ExpenseTracker.Config.EndpointConfig;
import com.myApp.ExpenseTracker.Config.EndpointResolver;
import com.myApp.ExpenseTracker.Config.KeyResolver;
import com.myApp.ExpenseTracker.Service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final KeyResolver keyResolver;
    private final EndpointResolver endpointResolver;
    private final RateLimiterService rateLimiterService;

    // Constructor injection for all dependencies
    public RateLimitInterceptor(KeyResolver keyResolver,
                                EndpointResolver endpointResolver,
                                RateLimiterService rateLimiterService) {
        this.keyResolver = keyResolver;
        this.endpointResolver = endpointResolver;
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws IOException {

        String key = keyResolver.resolveKey(request);
        EndpointConfig config = endpointResolver.resolve(request);

        boolean allowed = rateLimiterService.isAllowed(key, config);

        if (!allowed) {
            response.setStatus(429);
            response.getWriter().write("Too Many Requests");
            return false;
        }
        return true;
    }
}
