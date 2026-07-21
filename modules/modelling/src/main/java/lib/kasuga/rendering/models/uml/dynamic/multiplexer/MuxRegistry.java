package lib.kasuga.rendering.models.uml.dynamic.multiplexer;

import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Per-block-type cache of {@link Multiplexer} instances. Because a multiplexer is stateless, one
 * shared instance safely serves every placed block of that type. Definitions are immutable, so the
 * cache is safe for the Java source today; a clear hook is reserved for when the JSON source lands.
 */
public final class MuxRegistry {

    private final ConcurrentHashMap<ResourceLocation, Multiplexer> byBlockType = new ConcurrentHashMap<>();

    public Multiplexer resolve(ResourceLocation blockId, Function<ResourceLocation, Multiplexer> factory) {
        return byBlockType.computeIfAbsent(blockId, factory);
    }

    public void register(ResourceLocation blockId, Multiplexer multiplexer) {
        byBlockType.put(blockId, multiplexer);
    }

    public void clear() {
        byBlockType.clear();
    }
}
