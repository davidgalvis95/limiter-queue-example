package limiter.limiterqueueexample.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaDelayedRequestsConfig {

    public static final String DELAYED_REQUESTS_TOPIC = "test_topic";

    @Bean
    public NewTopic createTopic() {
        return new NewTopic(DELAYED_REQUESTS_TOPIC, 3, (short) 1);
    }
}
