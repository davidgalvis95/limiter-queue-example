package limiter.limiterqueueexample.service;

import java.util.concurrent.atomic.AtomicInteger;

public interface StateService {
    AtomicInteger getCurrentEnqueuedRequests();

    AtomicInteger getPostmanApiSentRequests();

    int increaseCurrentEnqueuedRequests();

    int decreaseCurrentEnqueuedRequests();

    int increasePostmanApiSentRequests();
}
