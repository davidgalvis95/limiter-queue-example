package limiter.limiterqueueexample.service;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/*
 * This is a control service for tracking some metrics of the API related to the requests sent and enqueued
 * */
@Getter
@Component
public class StateServiceImpl implements StateService {

    //If the currentEnqueuedRequests results to be negative, this means that there were added more new messages to the broker than the number of
    //requests to enqueue that this API instance has sent
    private AtomicInteger currentSentToQueueRequests = new AtomicInteger(0);

    private AtomicInteger currentPolledFromQueueRequests = new AtomicInteger(0);

    private AtomicInteger postmanApiSentRequests = new AtomicInteger(0);

    public StateServiceImpl() {
    }

    ;

    public StateServiceImpl(int initialSentToQueue, int initialSent, int initialPolledFromQueue) {
        this.currentSentToQueueRequests = new AtomicInteger(initialSentToQueue);
        this.postmanApiSentRequests = new AtomicInteger(initialSent);
        this.currentPolledFromQueueRequests = new AtomicInteger(initialPolledFromQueue);
    }

    @Override
    public int increaseSentToQueueRequests() {
        return currentSentToQueueRequests.incrementAndGet();
    }

    @Override
    public int decreaseSentToQueueRequests() {
        return currentSentToQueueRequests.decrementAndGet();
    }

    @Override
    public int increasePolledFromQueueRequests() {
        return currentPolledFromQueueRequests.incrementAndGet();
    }

    @Override
    public int decreasePolledFromQueueRequests() {
        return currentSentToQueueRequests.decrementAndGet();
    }

    @Override
    public int increasePostmanApiSentRequests() {
        return postmanApiSentRequests.incrementAndGet();
    }
}
