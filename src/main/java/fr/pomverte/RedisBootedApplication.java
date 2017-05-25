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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
			// send a message to the redis-server on startup
			template.convertAndSend("chat", "Hello from Redis!");

			template.opsForValue().set("winter", "For the Night is dark and full of terror");

			final String kingsKey = "kings";
			template.opsForList().leftPush(kingsKey, "Robert Baratheon");
			template.opsForList().leftPush(kingsKey, "Aegon Targaryen");
			template.opsForList().leftPush(kingsKey, "Joffrey Baratheon");

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

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/messages")
class MessageController {

	private StringRedisTemplate template;
	private CountDownLatch latch;

	/** send a message to a channel on redis-server */
	@PostMapping("/")
    public ResponseEntity<String> sendToChannel(@RequestParam("message") String message) {
		this.template.convertAndSend("chat", message);
		try {
			this.latch.await();
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
			return new ResponseEntity<>("oups :'(", HttpStatus.I_AM_A_TEAPOT);
		}
        return new ResponseEntity<>("all good ;)", HttpStatus.OK);
    }

	/** retreive message from redis-server */
	@GetMapping("/get/{key}")
	public ResponseEntity<String> getValueForKey(@PathVariable("key") String key) {
		return new ResponseEntity<>(this.template.opsForValue().get(key), HttpStatus.OK);
	}

	/** update/create message from redis-server */
	@PutMapping("/get/{key}/{value}")
	public ResponseEntity<String> putValueForKey(@PathVariable("key") String key, @PathVariable("value") String value) {
		this.template.opsForValue().set(key, value);
		return new ResponseEntity<>("updated", HttpStatus.OK);
	}

	@GetMapping("/kings")
	public ResponseEntity<String> getKings() {
		return new ResponseEntity<>(this.template.opsForList().leftPop("kings"), HttpStatus.OK);
	}
}
