package limiter.limiterqueueexample.config;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.internal.AtomicRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimiterConfig {

    @Bean
    public AtomicRateLimiter atomicRateLimiter() {
        io.github.resilience4j.ratelimiter.RateLimiterConfig config = io.github.resilience4j.ratelimiter.RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMillis(10000))
                .limitForPeriod(5)
                .timeoutDuration(Duration.ofMillis(0))
                .build();

        RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.of(config);

        final AtomicRateLimiter rateLimiterWithCustomConfig = (AtomicRateLimiter) rateLimiterRegistry
                .rateLimiter("name2", config);

        return rateLimiterWithCustomConfig;
    }
}
