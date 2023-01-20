package limiter.limiterqueueexample.config;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.internal.AtomicRateLimiter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Data
@Slf4j
@Configuration
@ConfigurationProperties("ratelimiter")
public class RateLimiterConfig {

    private Integer limitRefreshPeriod;

    private Integer limitForPeriod;

    private Integer timeoutDuration;

    @Bean
    public AtomicRateLimiter atomicRateLimiter() {
        io.github.resilience4j.ratelimiter.RateLimiterConfig config = io.github.resilience4j.ratelimiter.RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMillis(limitRefreshPeriod))
                .limitForPeriod(limitForPeriod)
                .timeoutDuration(Duration.ofMillis(timeoutDuration))
                .build();


        RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.of(config);
        return (AtomicRateLimiter) rateLimiterRegistry.rateLimiter("name2", config);
    }
}
