package limiter.limiterqueueexample.kafka.consumer;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.internal.AtomicRateLimiter;
import limiter.limiterqueueexample.service.ServiceRateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static limiter.limiterqueueexample.config.KafkaDelayedRequestsConfig.DELAYED_REQUESTS_TOPIC;

@Slf4j
@Component
public class DelayedRequestsConsumer implements AcknowledgingMessageListener<String, String>, ConsumerSeekAware{

    private final AtomicRateLimiter.AtomicRateLimiterMetrics metrics;

    private AtomicLong offsetToSeekFrom = new AtomicLong(-1L);

    private ConsumerSeekAware.ConsumerSeekCallback consumerSeekCallback;

    private ServiceRateLimiter serviceRateLimiter;

    @Autowired
    public DelayedRequestsConsumer(final RateLimiter rateLimiter, final ServiceRateLimiter serviceRateLimiter) {
        this.metrics = (AtomicRateLimiter.AtomicRateLimiterMetrics) rateLimiter.getMetrics();
        this.serviceRateLimiter = serviceRateLimiter;
    }

    @Override
    @KafkaListener(topics = DELAYED_REQUESTS_TOPIC, groupId = "group_id")
    public void onMessage(ConsumerRecord<String, String> consumerRecord, Acknowledgment acknowledgment) {
        if(metrics.getNanosToWait() <= 0) {
            log.info("RESENDING REQUEST -> id: " + consumerRecord.key() + ", value: " +  consumerRecord.value() + ", timestamp: " + LocalTime.now());
            serviceRateLimiter.sendRequest(UUID.fromString(consumerRecord.key()), consumerRecord.value());
            acknowledgment.acknowledge();
        }else {
            if(offsetToSeekFrom.get() != -1L) {
                offsetToSeekFrom = new AtomicLong(consumerRecord.offset());
            }else {
                offsetToSeekFrom = new AtomicLong(-1L);
                consumerSeekCallback.seekRelative(DELAYED_REQUESTS_TOPIC, consumerRecord.partition(), offsetToSeekFrom.get(), true);
            }
        }
    }

    @Override
    public void registerSeekCallback(final ConsumerSeekCallback consumerSeekCallback) {
        this.consumerSeekCallback = consumerSeekCallback;
    }

    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> map, ConsumerSeekCallback consumerSeekCallback) {
    }

    @Override
    public void onIdleContainer(Map<TopicPartition, Long> map, ConsumerSeekCallback consumerSeekCallback) {
    }
}
