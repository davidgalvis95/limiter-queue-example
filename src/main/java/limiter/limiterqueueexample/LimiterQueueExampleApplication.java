package limiter.limiterqueueexample;

import limiter.limiterqueueexample.config.KafkaDelayedRequestsConfig;
import limiter.limiterqueueexample.controller.LimiterQueueExampleController;
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

    private final LimiterQueueExampleController limiterQueueExampleController;

    @Autowired
    public LimiterQueueExampleApplication(final DelayedRequestsConsumer delayedRequestsManualConsumer,
                                          final LimiterQueueExampleController limiterQueueExampleController){
        this.delayedRequestsManualConsumer = delayedRequestsManualConsumer;
        this.limiterQueueExampleController = limiterQueueExampleController;
    }

    public static void main(String[] args) {
        SpringApplication.run(LimiterQueueExampleApplication.class, args);
    }

    /*
     * This following block (lines 42 to 44) should be commented when trying to start the application normally
     * This is only for demonstration purposes, look at the src/main/java/test.txt file to take a look to the results
     * */
    @Override
    public void run(String... args) {
        delayedRequestsManualConsumer.startBySubscribing();
//        for (int i=0; i<30; i++) {
//            limiterQueueExampleController.createOrder();
//        }
    }
}
