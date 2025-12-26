package scheduler.model;

public class Burst {
    public enum Type {CPU, IO}

    private final Type type;
    private final int duration; // in ticks

    public Burst(Type type, int duration) {
        if (duration < 0)
            throw new IllegalArgumentException("duration must be >= 0");
        this.type = type;
        this.duration = duration;
    }

    public Type getType() {
        return type;
    }

    public int getDuration() {
        return duration;
    }
}

