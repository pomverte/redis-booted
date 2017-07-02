package fr.pomverte;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class RedisBootedApplication {

	@Bean
	public CommandLineRunner commandLineRunner(StringRedisTemplate template, CountDownLatch latch) {
		return args -> {
			// send a message to the redis-server on startup
			template.convertAndSend("chat", "Hello from Redis!");

			template.opsForValue().set("winter", "For the Night is dark and full of terror");

			final String kingsKey = "kings";
			template.opsForList().leftPush(kingsKey, "Robert Baratheon");
			template.opsForList().leftPush(kingsKey, "Aegon Targaryen");
			template.opsForList().leftPush(kingsKey, "Joffrey Baratheon");

			// shadow will be evicted after 5 sec
			template.opsForValue().set("shadow", "I am no one");
			template.expire("shadow", 5, TimeUnit.SECONDS);

			latch.await();
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(RedisBootedApplication.class, args);
	}

}
