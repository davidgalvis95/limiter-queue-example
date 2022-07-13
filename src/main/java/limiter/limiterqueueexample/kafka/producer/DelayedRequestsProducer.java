package limiter.limiterqueueexample.kafka.producer;

import limiter.limiterqueueexample.service.StateService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static limiter.limiterqueueexample.config.KafkaDelayedRequestsConfig.DELAYED_REQUESTS_TOPIC;


@Slf4j
@Component
@AllArgsConstructor
public class DelayedRequestsProducer {

    private KafkaTemplate<String, String> kafkaTemplate;

    private DelayedProducerRequestsFuture delayedProducerRequestsFuture;

    public void sendMessage(final UUID key, final String value) {

        final ProducerRecord<String, String> producerRecord = buildProducerRecord(key.toString(), value, DELAYED_REQUESTS_TOPIC);
        final ListenableFuture<SendResult<String,String>> listenableFuture =  kafkaTemplate.send(producerRecord);
        delayedProducerRequestsFuture.setKeyAndValue(key, value);
        listenableFuture.addCallback(delayedProducerRequestsFuture);
    }

    private ProducerRecord<String, String> buildProducerRecord(final String key, final String value, final String topic) {
        final List<Header> recordHeaders = List.of(new RecordHeader("event-source", "scanner".getBytes()));
        return new ProducerRecord<>(topic, null, key, value, recordHeaders);
    }
}
