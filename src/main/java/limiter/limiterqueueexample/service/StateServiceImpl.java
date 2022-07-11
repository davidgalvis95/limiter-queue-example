package limiter.limiterqueueexample.service;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Component
public class StateServiceImpl implements StateService{

    private final AtomicInteger currentEnqueuedRequests = new AtomicInteger(0);

    private final AtomicInteger postmanApiSentRequests = new AtomicInteger(0);

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
