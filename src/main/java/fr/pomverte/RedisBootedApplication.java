package fr.pomverte;

import java.util.concurrent.CountDownLatch;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
public class RedisBootedApplication {

	/** A message listener container */
	@Bean
	public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
			MessageListenerAdapter listenerAdapter) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
        // the msg listener adapter is listening at the chat topic
        // tip : a regexp could be passed
		container.addMessageListener(listenerAdapter, new PatternTopic("chat"));

		return container;
	}

	/** A message listene adapter */
	@Bean
	public MessageListenerAdapter listenerAdapter(Receiver receiver) {
		// configured to call the Receiver.receiveMessage()
		return new MessageListenerAdapter(receiver, "receiveMessage");
	}

	@Bean
	public Receiver receiver(CountDownLatch latch) {
		return new Receiver(latch);
	}

	@Bean
	public CountDownLatch latch() {
		return new CountDownLatch(1);
	}

	@Bean
	public StringRedisTemplate template(RedisConnectionFactory connectionFactory) {
		return new StringRedisTemplate(connectionFactory);
	}

	@Bean
	public CommandLineRunner commandLineRunner (StringRedisTemplate template, CountDownLatch latch) {
		return args -> {
			template.convertAndSend("chat", "Hello from Redis!");
			latch.await();
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(RedisBootedApplication.class, args);
	}

}

@Slf4j
@Component
@AllArgsConstructor
class Receiver {

	private CountDownLatch latch;

	public void receiveMessage(String message) {
		log.info("Received '{}'", message);
		this.latch.countDown();
	}
}
