package limiter.limiterqueueexample.service;

import java.util.UUID;

public interface PostmanEchoApiService {

    void sendRequest(final UUID id, final String value);

}
