package com.myApp.ExpenseTracker.Config;


import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    private boolean enabled;
    private List<EndpointConfig> endpoints;

    @PostConstruct
    public void init() {
        if (endpoints != null) {
            endpoints.forEach(EndpointConfig::compilePatterns);
        }
    }
}