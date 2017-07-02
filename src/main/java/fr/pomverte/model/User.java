package fr.pomverte.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class User {
    private UUID uuid = UUID.randomUUID();
    private String name;

    public User(final String name) {
        this.uuid = UUID.randomUUID();
        this.name = name;
    }
}