package limiter.limiterqueueexample;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
public class LimiterQueueExampleApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(LimiterQueueExampleApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
//        System.out.println("Hiiii!");
    }
}
