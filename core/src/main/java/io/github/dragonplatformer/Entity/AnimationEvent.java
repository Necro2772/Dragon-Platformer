package io.github.dragonplatformer.Entity;

public class AnimationEvent {
    public final float time;
    public final AnimationEventKey event;

    public AnimationEvent(float time, AnimationEventKey event) {
        this.time = time;
        this.event = event;
    }

    public String toString() {
        return String.format("%.2f: %s", time, event);
    }
}
