package lib.kasuga.registration.data_driven.property.compiler;

import com.google.gson.JsonElement;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class ModifierCompiler {

    protected final BiPredicate<String, JsonElement> keyPredicate;
    protected final BiFunction<String, JsonElement, Consumer<BlockBehaviour.Properties>> sup;

    public ModifierCompiler(BiPredicate<String, JsonElement> keyPredicate, BiFunction<String, JsonElement, Consumer<BlockBehaviour.Properties>> supplier) {
        this.keyPredicate = keyPredicate;
        this.sup = supplier;
    }

    public boolean valid(String key, JsonElement value) {
        return keyPredicate.test(key,  value);
    }

    public @Nullable Consumer<BlockBehaviour.Properties> parse(String key, JsonElement value) {
        if (valid(key, value)) {return sup.apply(key, value);}
        return null;
    }

    public BiFunction<String, JsonElement, Consumer<BlockBehaviour.Properties>> getSupplier() {
        return sup;
    }

    public BiPredicate<String, JsonElement> getKeyPredicate() {
        return keyPredicate;
    }
}
