package limiter.limiterqueueexample.service;

import feign.FeignException;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.vavr.CheckedFunction0;
import limiter.limiterqueueexample.client.PostmanEchoApiClient;
import limiter.limiterqueueexample.kafka.producer.DelayedRequestsProducer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@AllArgsConstructor
public class PostmanEchoApiServiceImpl implements PostmanEchoApiService {

    private final DelayedRequestsProducer producer;

    private final PostmanEchoApiClient postmanEchoApiClient;

    private final RateLimiter rateLimiter;

    private final StateService stateService;

    @Override
    public void sendRequest(final UUID id, final String value) {
        final CheckedFunction0<CompletableFuture<Pair<UUID, String>>> responseSupplier = RateLimiter.decorateCheckedSupplier(rateLimiter, () -> sendApiRequest(id, value));
        try {
            responseSupplier.apply()
                    .thenAccept((res) -> {
                        final int sentRequests = stateService.increasePostmanApiSentRequests();
                        log.info("Request allowed and sent, id: {}, value: {}, timestamp: {}. Requests sent: {}",
                                res.getLeft(), res.getRight(), LocalDateTime.now(), sentRequests);
                    });
        } catch (final RequestNotPermitted e) {
            log.info("Queuing request not allowed for future resending, id: {}, value: {}, timestamp: {}", id, value, LocalDateTime.now());
            producer.sendMessage(id, value);
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }

    private CompletableFuture<Pair<UUID, String>> sendApiRequest(final UUID id, final String value) {
        return CompletableFuture
                .supplyAsync(() -> {
                    log.info("Sending request, id: {}, value: {}, timestamp: {}", id, value, LocalDateTime.now());
                    try {
                        postmanEchoApiClient.sendPostmanEchoRequest(id, value);
                    }catch (final FeignException.NotFound e) {
                        log.error("The api URL could not be found: 404"); //This error is expected since the endpoint always returns 404
                    }
                    return Pair.of(id, value);
                });
    }
}
