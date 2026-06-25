package lib.kasuga.registration.data_driven.property.compiler;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Locale;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class RLCompiler extends ModifierCompiler {

    protected final ResourceLocation id;

    public RLCompiler(ResourceLocation location, BiFunction<String, JsonElement, Consumer<BlockBehaviour.Properties>> supplier) {
        super((s, element) -> {
            ResourceLocation inputLoc = ResourceLocation.tryParse(s.toLowerCase(Locale.ROOT));
            if (inputLoc == null) {
                throw new IllegalArgumentException("Invalid block property key: " + s);
            }
            return Objects.equals(inputLoc, location);
        }, supplier);
        this.id = location;
    }

    public ResourceLocation getId() {
        return id;
    }
}
