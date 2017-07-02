package fr.pomverte.service;

public interface CacheService {

    void push(String channel, String message);

    void add(String key, String value);

    String get(String key);

    String read(String queue);
}
