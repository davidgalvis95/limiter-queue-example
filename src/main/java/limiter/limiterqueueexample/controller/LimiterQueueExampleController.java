package limiter.limiterqueueexample.controller;

import limiter.limiterqueueexample.service.PostmanEchoApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/v0/accept")
public class LimiterQueueExampleController {

    private final PostmanEchoApiService postmanEchoApiService;

    private final AtomicInteger requestCounter = new AtomicInteger();

    @Autowired
    public LimiterQueueExampleController(PostmanEchoApiService postmanEchoApiService) {
        this.postmanEchoApiService = postmanEchoApiService;
    }

    @PostMapping
    public ResponseEntity<Void> createOrder() {
        requestCounter.incrementAndGet();
        postmanEchoApiService.sendRequest(UUID.randomUUID(), "I'm the request #" + requestCounter + " :)");
        return ResponseEntity.ok().build();
    }
}
