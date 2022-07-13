package limiter.limiterqueueexample.kafka.producer;

import limiter.limiterqueueexample.service.StateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
public class DelayedProducerRequestsFuture implements ListenableFutureCallback<SendResult<String,String>> {

    private UUID key;

    private String value;

    private final StateService stateService;

    @Autowired
    public DelayedProducerRequestsFuture(final StateService stateService) {
        this.stateService = stateService;
    }

    public void setKeyAndValue(final UUID key, final String value){
        this.key = key;
        this.value = value;
    }

    @Override
    public void onFailure(Throwable ex) {
        log.error("Error sending the message with id: {} and value: {}, the exception is {}", key, value, ex.getMessage());
        try {
            throw ex;
        } catch (Throwable throwable) {
            log.error("Error in OnFailure: {}", throwable.getMessage());
        }
    }

    @Override
    public void onSuccess(SendResult<String, String> result) {
        final int sentToQueueRequests = stateService.increaseSentToQueueRequests();
        log.info("Request successFully enqueued for the key: {}, value: {}, timestamp: {}, partition: {}. Requests sent to queue: {}",
                key, value, LocalDateTime.now(), result.getRecordMetadata().partition(), sentToQueueRequests);
    }
}
