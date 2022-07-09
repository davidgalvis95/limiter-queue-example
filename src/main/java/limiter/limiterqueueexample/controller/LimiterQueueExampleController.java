package limiter.limiterqueueexample.controller;

import limiter.limiterqueueexample.kafka.producer.DelayedRequestsProducer;
import limiter.limiterqueueexample.service.ServiceRateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v0/accept")
public class LimiterQueueExampleController {

    private final ServiceRateLimiter serviceRateLimiter;

    private int counter;

    @Autowired
    public LimiterQueueExampleController(ServiceRateLimiter serviceRateLimiter) {
        this.serviceRateLimiter = serviceRateLimiter;
    }

    @PostMapping
    public ResponseEntity<Void> createOrder() {
        counter++;
        serviceRateLimiter.sendRequest(UUID.randomUUID(), "Hi" + counter + " :)");
        return ResponseEntity.ok().build();
    }
}
