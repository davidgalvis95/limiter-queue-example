package limiter.limiterqueueexample.producer;

import limiter.limiterqueueexample.kafka.producer.DelayedProducerRequestsFuture;
import limiter.limiterqueueexample.kafka.producer.DelayedRequestsProducer;
import limiter.limiterqueueexample.service.StateService;
import limiter.limiterqueueexample.service.StateServiceImpl;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.List;
import java.util.UUID;

import static limiter.limiterqueueexample.config.KafkaDelayedRequestsConfig.DELAYED_REQUESTS_TOPIC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DelayedRequestsProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private DelayedProducerRequestsFuture delayedProducerRequestsFuture;

    @InjectMocks
    private DelayedRequestsProducer delayedRequestsProducer;



    @BeforeEach
    void setUp(){
    }

    @Test
    void testProducerDecrementsCount(){
        final SettableListenableFuture<SendResult<String, String>> future = new SettableListenableFuture<>();

        final UUID rec1Id = UUID.randomUUID();
        final UUID rec2Id = UUID.randomUUID();
        final UUID rec3Id = UUID.randomUUID();

        final ProducerRecord<String, String> rec1 = buildProducerRecord(String.valueOf(rec1Id), "Request 4");
        final ProducerRecord<String, String> rec2 = buildProducerRecord(String.valueOf(rec2Id), "Request 5");
        final ProducerRecord<String, String> rec3 = buildProducerRecord(String.valueOf(rec3Id), "Request 6");

        when(kafkaTemplate.send(eq(rec1))).thenReturn(future);
        when(kafkaTemplate.send(eq(rec2))).thenReturn(future);
        when(kafkaTemplate.send(eq(rec3))).thenReturn(future);

        delayedRequestsProducer.sendMessage(rec1Id, "Request 4");
        delayedRequestsProducer.sendMessage(rec2Id, "Request 5");
        delayedRequestsProducer.sendMessage(rec3Id, "Request 6");

        verify(kafkaTemplate).send(eq(rec1));
        verify(kafkaTemplate).send(eq(rec2));
        verify(kafkaTemplate).send(eq(rec3));

        verify(delayedProducerRequestsFuture, times(3)).setKeyAndValue(any(), anyString());
    }

    private static ProducerRecord<String, String> buildProducerRecord(final String key, final String value) {
        final List<Header> recordHeaders = List.of(new RecordHeader("event-source", "scanner".getBytes()));
        return new ProducerRecord<>(DELAYED_REQUESTS_TOPIC, null, key, value, recordHeaders);
    }
}
