package limiter.limiterqueueexample.client;

import feign.Headers;
import feign.RequestLine;


@Headers({"Accept: application/json",
        "Content-Type: application/json"})
public interface PostmanEchoApiClient {

    @RequestLine("GET /docs/developer/echo-api/")
    void sendPostmanEchoRequest();
}
