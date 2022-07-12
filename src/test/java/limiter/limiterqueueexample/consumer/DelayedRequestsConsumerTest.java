package limiter.limiterqueueexample.consumer;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.internal.AtomicRateLimiter;
import limiter.limiterqueueexample.client.PostmanEchoApiClient;
import limiter.limiterqueueexample.kafka.consumer.DelayedRequestsConsumer;
import limiter.limiterqueueexample.kafka.producer.DelayedRequestsProducer;
import limiter.limiterqueueexample.service.PostmanEchoApiServiceImpl;
import limiter.limiterqueueexample.service.StateService;
import limiter.limiterqueueexample.service.StateServiceImpl;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.time.Duration;
import java.util.*;

import static limiter.limiterqueueexample.config.KafkaDelayedRequestsConfig.DELAYED_REQUESTS_TOPIC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DelayedRequestsConsumerTest {

    private MockConsumer<String, String> mockConsumer;

    private StateService stateService;

    @Mock
    private DelayedRequestsProducer producer;

    @Mock
    private PostmanEchoApiClient postmanEchoApiClient;

    @Mock
    private File selectedMessagesFile;

    private PostmanEchoApiServiceImpl postmanEchoApiService;

    private DelayedRequestsConsumer delayedRequestsConsumer;

    private final static Integer KAFKA_POLLING_DURATION = 15000;

    @BeforeEach
    void setUp(){
        stateService = new StateServiceImpl(5, 0);
        mockConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        RateLimiter rateLimiter = buildAtomicRateLimiter();
        postmanEchoApiService = new PostmanEchoApiServiceImpl(producer, postmanEchoApiClient, rateLimiter, stateService, selectedMessagesFile);
        delayedRequestsConsumer = new DelayedRequestsConsumer(mockConsumer, rateLimiter, postmanEchoApiService, stateService, KAFKA_POLLING_DURATION);
    }

    @Test
    public void consumerTest() {
        final TopicPartition topicPartition = new TopicPartition(DELAYED_REQUESTS_TOPIC, 0);
        mockConsumer.schedulePollTask(() -> addTopicPartitions(mockConsumer, topicPartition));
        mockConsumer.schedulePollTask(delayedRequestsConsumer::shutdown);
        delayedRequestsConsumer.startBySubscribing();

        verify(postmanEchoApiClient, times(3)).sendPostmanEchoRequest();
        verify(producer).sendMessage(any(), eq("Request 4"));
        verify(producer).sendMessage(any(), eq("Request 5"));

        assertEquals(3, stateService.getPostmanApiSentRequests().get());
        assertEquals(0, stateService.getCurrentEnqueuedRequests().get());
    }

    private void addTopicPartitions(final MockConsumer<String, String> mockConsumer,
                                    final TopicPartition topicPartition) {

        final Map<TopicPartition, Long> beginningOffsets = new HashMap<>();
        beginningOffsets.put(topicPartition, 0L);
        mockConsumer.rebalance(Collections.singletonList(topicPartition));
        mockConsumer.updateBeginningOffsets(beginningOffsets);
        addConsumerRecords(topicPartition);
    }

    private void addConsumerRecords(final TopicPartition topicPartition) {
        mockConsumer.addRecord(new ConsumerRecord<>(DELAYED_REQUESTS_TOPIC, topicPartition.partition(), 0, UUID.randomUUID().toString(), "Request 1"));
        mockConsumer.addRecord(new ConsumerRecord<>(DELAYED_REQUESTS_TOPIC, topicPartition.partition(), 1, UUID.randomUUID().toString(), "Request 2"));
        mockConsumer.addRecord(new ConsumerRecord<>(DELAYED_REQUESTS_TOPIC, topicPartition.partition(), 2, UUID.randomUUID().toString(), "Request 3"));
        mockConsumer.addRecord(new ConsumerRecord<>(DELAYED_REQUESTS_TOPIC, topicPartition.partition(), 3, UUID.randomUUID().toString(), "Request 4"));
        mockConsumer.addRecord(new ConsumerRecord<>(DELAYED_REQUESTS_TOPIC, topicPartition.partition(), 4, UUID.randomUUID().toString(), "Request 5"));
    }

    public AtomicRateLimiter buildAtomicRateLimiter() {
        io.github.resilience4j.ratelimiter.RateLimiterConfig config = io.github.resilience4j.ratelimiter.RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMillis(5000))
                .limitForPeriod(3)
                .timeoutDuration(Duration.ofMillis(0))
                .build();

        RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.of(config);
        return (AtomicRateLimiter) rateLimiterRegistry.rateLimiter("rateLimiter", config);
    }
}
