package limiter.limiterqueueexample.kafka.consumer;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.internal.AtomicRateLimiter;
import limiter.limiterqueueexample.service.PostmanEchoApiService;
import limiter.limiterqueueexample.service.StateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static limiter.limiterqueueexample.config.KafkaDelayedRequestsConfig.DELAYED_REQUESTS_TOPIC;

@Slf4j
@Component
public class DelayedRequestsConsumer {

    private volatile boolean keepConsuming = true;

    private final AtomicRateLimiter.AtomicRateLimiterMetrics metrics;

    private final Consumer<String, String> consumer;

    private final PostmanEchoApiService postmanEchoApiService;

    private final StateService stateService;

    private final Integer kafkaPollingDuration;

    @Autowired
    public DelayedRequestsConsumer(final Consumer<String, String> manualKafkaConsumer,
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

    public void startBySubscribing() {
        onMessage(() -> {
            consumer.subscribe(Collections.singleton(DELAYED_REQUESTS_TOPIC));
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        });
    }

    public void onMessage(Runnable beforePollingTask) {
        try {
            beforePollingTask.run();
            consume();
        } catch (final WakeupException e) {
            log.info("Wake up exception");
        } catch (final Exception e) {
            log.error("Unexpected exception", e);
        } finally {
            consumer.close();
            log.info("DelayedRequestsConsumer has been closed");
        }
    }

    private void consume() {
        while (keepConsuming) {
            if (metrics.getNanosToWait() <= 0) {
                final ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(kafkaPollingDuration));
                for (final ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                    this.processRecord(consumerRecord.key(), consumerRecord.value());
                }
            }
        }
    }

    public void processRecord(final String key, String value) {
        final int currentEnqueuedReq = stateService.decreaseCurrentEnqueuedRequests();
        log.info("Resending failed request with id: {}, value: {}, timestamp: {}. Enqueued requests {}",
                key, value, LocalDateTime.now(), currentEnqueuedReq);
        postmanEchoApiService.sendRequest(UUID.fromString(key), value);
    }

    public void shutdown() {
        log.info("Shutting down DelayedRequestsConsumer");
        keepConsuming = false;
        consumer.wakeup();
    }
}
