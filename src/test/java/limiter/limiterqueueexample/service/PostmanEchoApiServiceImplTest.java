package limiter.limiterqueueexample.service;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.internal.AtomicRateLimiter;
import limiter.limiterqueueexample.client.PostmanEchoApiClient;
import limiter.limiterqueueexample.kafka.producer.DelayedRequestsProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.time.Duration;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class PostmanEchoApiServiceImplTest {

    @Mock
    private DelayedRequestsProducer producer;

    @Mock
    private PostmanEchoApiClient postmanEchoApiClient;

    @Mock
    private StateService stateService;

    @Mock
    private File selectedMessagesFile;

    private PostmanEchoApiServiceImpl postmanEchoApiService;

    @BeforeEach
    void setUp(){
        RateLimiter rateLimiter = buildAtomicRateLimiter();
        postmanEchoApiService = new PostmanEchoApiServiceImpl(producer, postmanEchoApiClient, rateLimiter, stateService, selectedMessagesFile);
    }

    @Test
    void sending10WhenAllowed5Each10Seconds() {
        for (int i = 1; i <= 20; i++) {
            postmanEchoApiService.sendRequest(UUID.randomUUID(), "Request " + i);
        }
        verify(producer, times(15)).sendMessage(any(), anyString());
        verify(postmanEchoApiClient, times(5)).sendPostmanEchoRequest();
    }

    public AtomicRateLimiter buildAtomicRateLimiter() {
        io.github.resilience4j.ratelimiter.RateLimiterConfig config = io.github.resilience4j.ratelimiter.RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMillis(10000))
                .limitForPeriod(5)
                .timeoutDuration(Duration.ofMillis(0))
                .build();

        RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.of(config);
        return (AtomicRateLimiter) rateLimiterRegistry.rateLimiter("rateLimiter", config);
    }

}


