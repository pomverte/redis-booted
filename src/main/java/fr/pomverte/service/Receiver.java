package fr.pomverte.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Slf4j
@Component
@AllArgsConstructor
public class Receiver {

    private CountDownLatch latch;

    public void receiveMessage(String message) {
        log.info("Received '{}'", message);
        this.latch.countDown();
    }
}
