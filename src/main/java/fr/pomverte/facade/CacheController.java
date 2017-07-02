package fr.pomverte.facade;

import fr.pomverte.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/api")
public class CacheController {

    @GetMapping("/users/{name}")
    @Cacheable(cacheNames = "users", key = "#name")
    public User findUser(@PathVariable("name") String name) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        return new User(name);
    }

    @DeleteMapping("/users")
    @CacheEvict(cacheNames = "users", allEntries = true)
    public void purge() {
        log.warn("Purging users cache !");
    }
}
