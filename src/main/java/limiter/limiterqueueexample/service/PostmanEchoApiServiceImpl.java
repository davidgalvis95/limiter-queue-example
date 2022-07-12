package limiter.limiterqueueexample.service;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.vavr.CheckedFunction0;
import limiter.limiterqueueexample.client.PostmanEchoApiClient;
import limiter.limiterqueueexample.kafka.producer.DelayedRequestsProducer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.io.*;
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

    private final File selectedMessagesFile;

    @Override
    public void sendRequest(final UUID id, final String value) {
        final CheckedFunction0<CompletableFuture<Pair<UUID, String>>> responseSupplier = RateLimiter.decorateCheckedSupplier(rateLimiter, () -> sendApiRequest(id, value));
        try {
            responseSupplier.apply()
                    .thenAccept((res) -> {
                        final int sentRequests = stateService.increasePostmanApiSentRequests();
                        log.info("Request allowed and sent, id: {}, value: {}, timestamp: {}. Requests sent: {}",
                                res.getLeft(), res.getRight(), LocalDateTime.now(), sentRequests);
                        writeToFile(res, sentRequests);
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
                        postmanEchoApiClient.sendPostmanEchoRequest();
                    } catch (final Exception e) {
                        throw new RuntimeException("Something went wrong");
                    }
                    return Pair.of(id, value);
                });
    }

    private void writeToFile(final Pair<UUID, String> res, final int sentRequests) {
        try {
            Writer output;
            output = new BufferedWriter(new FileWriter(selectedMessagesFile.getAbsolutePath(), true));
            final int enqueuedRequests = stateService.getCurrentEnqueuedRequests().get();
            output.append(String.format("id: %s, value: %s, timestamp: %s, Requests sent: %s, Enqueued requests: %s\n",
                    res.getLeft().toString(), res.getRight(), LocalDateTime.now(), sentRequests, enqueuedRequests));
            output.close();
        } catch (IOException e) {
            log.error("Error when trying to write to the file");
        }
    }
}
