package limiter.limiterqueueexample.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Properties;

@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "spring.kafka.consumer")
public class KafkaDelayedRequestsConfig {

    private String bootstrapServers;

    private String groupId;

    private String autoOffsetReset;

    private String maxPollRecords;

    public static final String DELAYED_REQUESTS_TOPIC = "test_topic";

    @Bean
    public NewTopic createTopic() {
        return new NewTopic(DELAYED_REQUESTS_TOPIC, 3, (short) 1);
    }

    @Bean
    public KafkaConsumer<String, String> manualKafkaConsumer(final NewTopic topic){
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);

        final KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(List.of(topic.name()));
        createConsumerShutdownHook(consumer);
        return consumer;
    }

    public void createConsumerShutdownHook(final KafkaConsumer<String, String> manualKafkaConsumer){
        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Calling manualKafkaConsumer.wakeup() due to JVM shutdown");
            manualKafkaConsumer.wakeup();
            try {
                mainThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
    }
}
