package lib.kasuga.rendering.models.uml.dynamic.multiplexer;

import lib.kasuga.rendering.models.uml.dynamic.data.Blackboard;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/** Static factories for common {@link MultiplexerInput} predicates. */
public final class MuxPredicateContext {

    private MuxPredicateContext() {}

    public static Predicate<MultiplexerInput> propertyIs(String name, String value) {
        return input -> input.propertyIs(name, value);
    }

    public static Predicate<MultiplexerInput> powerAtLeast(int min) {
        return input -> input.redstonePower() >= min;
    }

    public static Predicate<MultiplexerInput> hasTag(ResourceLocation tag) {
        return input -> input.tags().contains(tag);
    }

    /** {@code direction} is a 0..5 index into the neighbor list (down/up/north/south/west/east). */
    public static Predicate<MultiplexerInput> neighborIs(int direction, String blockId) {
        return input -> {
            List<String> neighbors = input.neighbors();
            if (direction < 0 || direction >= neighbors.size()) {
                return false;
            }
            return blockId.equals(neighbors.get(direction));
        };
    }

    /** Match a typed custom channel on the input's {@link Blackboard} (a {@code Boolean} key). */
    public static Predicate<MultiplexerInput> dataFlag(Blackboard.Key<Boolean> key) {
        return input -> Boolean.TRUE.equals(input.data().get(key));
    }

    /** Match a typed custom channel on the input's {@link Blackboard} by equality. */
    public static <T> Predicate<MultiplexerInput> dataEquals(Blackboard.Key<T> key, T expected) {
        return input -> Objects.equals(expected, input.data().get(key));
    }

    /** Match a raw (string-named) custom channel by equality. */
    public static Predicate<MultiplexerInput> rawEquals(String name, Object expected) {
        return input -> Objects.equals(expected, input.data().get(name));
    }
}
