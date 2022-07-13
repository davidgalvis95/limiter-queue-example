package limiter.limiterqueueexample.service;

import java.util.concurrent.atomic.AtomicInteger;

public interface StateService {
    AtomicInteger getCurrentSentToQueueRequests();

    AtomicInteger getCurrentPolledFromQueueRequests();

    AtomicInteger getPostmanApiSentRequests();

    int increaseSentToQueueRequests();

    int decreaseSentToQueueRequests();

    int increasePolledFromQueueRequests();

    int decreasePolledFromQueueRequests();

    int increasePostmanApiSentRequests();
}
