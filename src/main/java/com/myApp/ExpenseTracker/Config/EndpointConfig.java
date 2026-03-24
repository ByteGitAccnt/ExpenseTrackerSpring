package com.myApp.ExpenseTracker.Config;

import com.myApp.ExpenseTracker.Utils.KeyType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EndpointConfig {

    private String name;
    private List<String> paths;
    private KeyType keyType;

    private long capacity;
    private long refillTokens;
    private long refillDuration;

    // Precompiled patterns (important for performance)
    private List<String> compiledPatterns;

    public void compilePatterns() {
        this.compiledPatterns = this.paths;
    }
}