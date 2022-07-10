package limiter.limiterqueueexample;

import limiter.limiterqueueexample.config.KafkaDelayedRequestsConfig;
import limiter.limiterqueueexample.kafka.consumer.DelayedRequestsConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties(KafkaDelayedRequestsConfig.class)
public class LimiterQueueExampleApplication implements CommandLineRunner {

    private final DelayedRequestsConsumer delayedRequestsManualConsumer;

    @Autowired
    public LimiterQueueExampleApplication(final DelayedRequestsConsumer delayedRequestsManualConsumer){
        this.delayedRequestsManualConsumer = delayedRequestsManualConsumer;
    }

    public static void main(String[] args) {
        SpringApplication.run(LimiterQueueExampleApplication.class, args);
    }

    @Override
    public void run(String... args) {
        delayedRequestsManualConsumer.onMessage();
    }
}
