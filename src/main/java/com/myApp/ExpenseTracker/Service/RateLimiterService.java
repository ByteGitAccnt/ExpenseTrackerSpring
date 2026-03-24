package com.myApp.ExpenseTracker.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.myApp.ExpenseTracker.Config.EndpointConfig;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimiterService {

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    public boolean isAllowed(String key, EndpointConfig config) {
        String finalKey = config.getName() + ":" + key;
        Bucket bucket = buckets.get(finalKey, k -> createBucket(config));
        return bucket.tryConsume(1);
    }

    private Bucket createBucket(EndpointConfig config) {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(config.getCapacity())
                        .refillGreedy(config.getRefillTokens(), Duration.ofSeconds(config.getRefillDuration())))
                .build();
    }

}