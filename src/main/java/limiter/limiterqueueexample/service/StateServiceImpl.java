package limiter.limiterqueueexample.service;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Component
public class StateServiceImpl implements StateService{

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
