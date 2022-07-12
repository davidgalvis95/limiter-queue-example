package limiter.limiterqueueexample.service;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/*
* This is a control service for tracking some metrics of the API related to the requests sent and enqueued
* */
@Getter
@Component
public class StateServiceImpl implements StateService{

    //If the currentEnqueuedRequests results to be negative, this means that there were added more new messages to the broker than the number of
    //requests to enqueue that this API instance has sent
    private AtomicInteger currentEnqueuedRequests = new AtomicInteger(0);

    private AtomicInteger postmanApiSentRequests = new AtomicInteger(0);

    public StateServiceImpl(){};

    public StateServiceImpl(int initialEnqueued, int initialSent) {
        this.currentEnqueuedRequests = new AtomicInteger(initialEnqueued);
        this.postmanApiSentRequests = new AtomicInteger(initialSent);
    }

    @Override
    public int increaseCurrentEnqueuedRequests() {
        return currentEnqueuedRequests.incrementAndGet();
    }

    @Override
    public int decreaseCurrentEnqueuedRequests() {
        return currentEnqueuedRequests.decrementAndGet();
    }

    @Override
    public int increasePostmanApiSentRequests() {
        return postmanApiSentRequests.incrementAndGet();
    }
}
