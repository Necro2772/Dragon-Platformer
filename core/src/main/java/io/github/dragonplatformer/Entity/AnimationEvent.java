package io.github.dragonplatformer.Entity;

public class AnimationEvent {
    public final float time;
    public final String event;

    public AnimationEvent(float time, String event) {
        this.time = time;
        this.event = event;
    }

    public String toString() {
        return String.format("%.2f: %s", time, event);
    }
}
