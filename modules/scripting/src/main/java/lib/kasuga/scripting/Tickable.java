package lib.kasuga.scripting;

public interface Tickable {
    void tick();
    default void close() {}
}
