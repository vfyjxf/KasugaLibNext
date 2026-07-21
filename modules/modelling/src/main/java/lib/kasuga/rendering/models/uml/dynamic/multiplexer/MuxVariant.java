package lib.kasuga.rendering.models.uml.dynamic.multiplexer;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * A variant node in a {@link Multiplexer} graph — a typed handle (the same role {@code State} plays
 * for the state machine). Built via {@code Multiplexer.builder().variant("id", v -> v.model(...))},
 * which returns this handle; transitions and {@link MuxState#current()} reference it by object, not string.
 */
public final class MuxVariant {

    private final String id;
    private ResourceLocation modelVariant;
    private ResourceLocation morphSet;
    private ResourceLocation overlay;

    MuxVariant(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public ResourceLocation modelVariant() {
        return modelVariant;
    }

    public @Nullable ResourceLocation morphSet() {
        return morphSet;
    }

    public @Nullable ResourceLocation overlay() {
        return overlay;
    }

    //region configuration (during build)

    public MuxVariant model(ResourceLocation modelVariant) {
        this.modelVariant = modelVariant;
        return this;
    }

    public MuxVariant morphSet(ResourceLocation morphSet) {
        this.morphSet = morphSet;
        return this;
    }

    public MuxVariant overlay(ResourceLocation overlay) {
        this.overlay = overlay;
        return this;
    }

    //endregion
}
