package lib.kasuga.registration.data_driven.property.compiler;

import com.google.gson.JsonElement;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class PropertyCompiler {

    protected final BiPredicate<String, JsonElement> keyPredicate;
    protected final BiFunction<String, JsonElement, Function<BlockBehaviour.Properties, BlockBehaviour.Properties>> sup;

    public PropertyCompiler(BiPredicate<String, JsonElement> keyPredicate, BiFunction<String, JsonElement, Function<BlockBehaviour.Properties, BlockBehaviour.Properties>> supplier) {
        this.keyPredicate = keyPredicate;
        this.sup = supplier;
    }

    public boolean valid(String key, JsonElement value) {
        return keyPredicate.test(key, value);
    }

    public @Nullable Function<BlockBehaviour.Properties, BlockBehaviour.Properties> parse(String key, JsonElement value) {
        if (valid(key, value)) { return sup.apply(key, value); }
        return null;
    }

    public BiFunction<String, JsonElement, Function<BlockBehaviour.Properties, BlockBehaviour.Properties>> getSupplier() {
        return sup;
    }

    public BiPredicate<String, JsonElement> getKeyPredicate() {
        return keyPredicate;
    }
}
