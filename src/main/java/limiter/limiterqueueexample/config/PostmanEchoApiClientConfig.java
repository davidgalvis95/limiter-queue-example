package limiter.limiterqueueexample.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.Request;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import limiter.limiterqueueexample.client.PostmanEchoApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class PostmanEchoApiClientConfig {

    private final String postmanEchoApiUrl;

    private final long connectionTimeout;

    private final long readTimeout;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    public PostmanEchoApiClientConfig(@Value("${postmanEchoApi.baseUrl}") final String postmanEchoApiUrl,
                                      @Value("${postmanEchoApi.timeout.connect}") final long connectionTimeout,
                                      @Value("${postmanEchoApi.timeout.read}") final long readTimeout) {
        this.postmanEchoApiUrl = postmanEchoApiUrl;
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
    }

    @Bean
    public PostmanEchoApiClient postmanEchoApiClient() {
        return Feign.builder()
                .options(new Request.Options(connectionTimeout, TimeUnit.MILLISECONDS, readTimeout, TimeUnit.MILLISECONDS, true))
                .encoder(new JacksonEncoder(OBJECT_MAPPER))
                .decoder(new JacksonDecoder(OBJECT_MAPPER))
                .target(PostmanEchoApiClient.class, postmanEchoApiUrl);
    }

    @Bean
    public File selectedMessagesFile() throws IOException {
        System.out.println();
        File file = new File(System.getProperty("user.dir") + "/src/main/resources/test.txt");
        if (file.createNewFile()) {
            log.info("File {} was created", file.getAbsolutePath());
        } else {
            log.info("File {} already exists", file.getAbsolutePath());
            String fileName = file.getAbsolutePath();
            try (BufferedWriter bf = Files.newBufferedWriter(Path.of(fileName),
                    StandardOpenOption.TRUNCATE_EXISTING)) {
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        file.setReadable(true, false);
        file.setExecutable(true, false);
        file.setWritable(true, false);
        return file;
    }
}
