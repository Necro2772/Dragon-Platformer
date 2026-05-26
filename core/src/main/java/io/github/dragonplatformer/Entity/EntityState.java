package io.github.dragonplatformer.Entity;

public interface EntityState {
    boolean isNonBlocking();
    EntityState nextState();
    String name();
}
