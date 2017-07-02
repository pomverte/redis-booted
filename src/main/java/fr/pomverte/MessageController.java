package fr.pomverte;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CountDownLatch;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/messages")
public class MessageController {

    private StringRedisTemplate stringRedisTemplate;
    private CountDownLatch latch;

    /** send a message to a channel on redis-server */
    @PostMapping("/")
    public ResponseEntity<String> sendToChannel(@RequestParam("message") String message) {
        this.stringRedisTemplate.convertAndSend("chat", message);
        try {
            this.latch.await();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>("oups :'(", HttpStatus.I_AM_A_TEAPOT);
        }
        return ResponseEntity.ok("all good ;)");
    }

    /** retreive message from redis-server */
    @GetMapping("/get/{key}")
    public ResponseEntity<String> getValueForKey(@PathVariable("key") String key) {
        return ResponseEntity.ok(this.stringRedisTemplate.opsForValue().get(key));
    }

    /** update/create message from redis-server */
    @PutMapping("/get/{key}/{value}")
    public ResponseEntity<String> putValueForKey(@PathVariable("key") String key, @PathVariable("value") String value) {
        this.stringRedisTemplate.opsForValue().set(key, value);
        return ResponseEntity.ok("updated");
    }

    @GetMapping("/kings")
    public ResponseEntity<String> getKings() {
        return ResponseEntity.ok(this.stringRedisTemplate.opsForList().leftPop("kings"));
    }
}
