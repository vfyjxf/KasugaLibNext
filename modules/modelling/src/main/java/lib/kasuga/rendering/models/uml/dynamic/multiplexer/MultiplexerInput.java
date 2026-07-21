package lib.kasuga.rendering.models.uml.dynamic.multiplexer;

import lib.kasuga.rendering.models.uml.dynamic.data.Blackboard;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Immutable, pure-data snapshot fed to a {@link Multiplexer}. No live Minecraft references — built
 * fresh per block update (neighbor / property / redstone change) and discarded after evaluation.
 *
 * <p>The built-in channels ({@code properties}/{@code neighbors}/{@code redstonePower}/{@code dayTime}/
 * {@code tags}) cover the common cases; {@link #data()} is an open {@link Blackboard} for any custom
 * input a modder needs (sensors, weather, biome, …) without editing this type.
 */
public record MultiplexerInput(
        Map<String, String> properties,
        List<String> neighbors,
        int redstonePower,
        long dayTime,
        Set<ResourceLocation> tags,
        Blackboard data
) {

    public MultiplexerInput {
        properties = Map.copyOf(properties);
        neighbors = List.copyOf(neighbors);
        tags = Set.copyOf(tags);
        data = data != null ? data : Blackboard.empty();
    }

    /** Convenience for the built-in channels only (custom data left empty). */
    public MultiplexerInput(Map<String, String> properties, List<String> neighbors,
                            int redstonePower, long dayTime, Set<ResourceLocation> tags) {
        this(properties, neighbors, redstonePower, dayTime, tags, Blackboard.empty());
    }

    public String property(String name) {
        return properties.get(name);
    }

    public boolean propertyIs(String name, String value) {
        return value.equals(properties.get(name));
    }
}
