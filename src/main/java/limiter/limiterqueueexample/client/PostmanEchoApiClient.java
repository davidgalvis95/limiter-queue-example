package limiter.limiterqueueexample.client;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.UUID;

@Headers({"Accept: application/json",
        "Content-Type: application/json"})
public interface PostmanEchoApiClient {

    @RequestLine("POST /post/{id}")
    void sendPostmanEchoRequest(@Param final UUID id, final String value);
}
