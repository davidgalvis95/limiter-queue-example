package limiter.limiterqueueexample.service;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.vavr.CheckedFunction0;
import limiter.limiterqueueexample.kafka.producer.DelayedRequestsProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class ServiceRateLimiter {

    private final DelayedRequestsProducer producer;

    final CheckedFunction0<CompletableFuture<Void>> responseSupplier;

    @Autowired
    public ServiceRateLimiter(final RateLimiter rateLimiter, final DelayedRequestsProducer producer) {
        this.producer = producer;
        this.responseSupplier = RateLimiter.decorateCheckedSupplier(rateLimiter, () -> sendRequest());
    }

    public void sendRequest(final UUID id, final String str) {
        try {
            responseSupplier.apply().thenAccept((response) -> log.info("REQUEST ALLOWED -> id: " + id + ", value: " + response + ", timestamp: " + LocalTime.now()));
        } catch (RequestNotPermitted e) {
            log.info("REQUEST NOT ALLOWED -> id: " + id + ", value: " + str + ", timestamp: " + LocalTime.now());
            producer.sendMessage(id, str);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private CompletableFuture<Void> sendRequest() {
        return CompletableFuture
                .runAsync(() -> {
                    //execute call to the API
                });
    }
}
