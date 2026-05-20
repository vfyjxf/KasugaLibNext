package lib.kasuga.registration.factory;

import lib.kasuga.registration.Reg;
import net.minecraft.world.level.block.Block;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FactoryRegistry {

    @FunctionalInterface
    public interface BlockFactory {
        Reg<?, Block> create(String id);
    }

    private static final Map<String, BlockFactory> REGISTRY = new ConcurrentHashMap<>();

    public static void register(String type, BlockFactory factory) {
        REGISTRY.put(type, factory);
    }

    public static BlockFactory get(String type) {
        return REGISTRY.get(type);
    }

    public static boolean contains(String type) {
        return REGISTRY.containsKey(type);
    }

    private FactoryRegistry() {}
}
