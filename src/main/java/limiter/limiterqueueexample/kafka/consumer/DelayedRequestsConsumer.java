package limiter.limiterqueueexample.kafka.consumer;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.internal.AtomicRateLimiter;
import limiter.limiterqueueexample.service.PostmanEchoApiService;
import limiter.limiterqueueexample.service.StateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class DelayedRequestsConsumer {

    private final AtomicRateLimiter.AtomicRateLimiterMetrics metrics;

    private final KafkaConsumer<String, String> consumer;

    private final PostmanEchoApiService postmanEchoApiService;

    private final StateService stateService;

    private final Integer kafkaPollingDuration;

    @Autowired
    public DelayedRequestsConsumer(final KafkaConsumer<String, String> manualKafkaConsumer,
                                   final RateLimiter rateLimiter,
                                   final PostmanEchoApiService postmanEchoApiService,
                                   final StateService stateService,
                                   @Value("${spring.kafka.consumer.pollingDuration}") final Integer kafkaPollingDuration) {
        this.consumer = manualKafkaConsumer;
        this.postmanEchoApiService = postmanEchoApiService;
        this.metrics = (AtomicRateLimiter.AtomicRateLimiterMetrics) rateLimiter.getMetrics();
        this.stateService = stateService;
        this.kafkaPollingDuration = kafkaPollingDuration;
    }

    public void onMessage() {
        CompletableFuture.runAsync(() -> {
            try {
                consume();
            } catch (final WakeupException e) {
                log.info("Wake up exception");
            } catch (final Exception e) {
                log.error("Unexpected exception", e);
            } finally {
                consumer.close();
                log.info("DelayedRequestsConsumer has been closed");
            }
        });
    }

    private void consume() {
        while (true) {
            if (metrics.getNanosToWait() <= 0) {
                final ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(kafkaPollingDuration));
                for (final ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                    final int currentEnqueuedReq = stateService.decreaseCurrentEnqueuedRequests();
                    log.info("Resending failed request with id: {}, value: {}, timestamp: {}. Enqueued requests {}",
                            consumerRecord.key(), consumerRecord.value(), LocalDateTime.now(), currentEnqueuedReq);
                    postmanEchoApiService.sendRequest(UUID.fromString(consumerRecord.key()), consumerRecord.value());
                }
            }
        }
    }
}
