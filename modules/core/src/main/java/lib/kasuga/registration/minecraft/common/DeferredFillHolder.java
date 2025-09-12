package lib.kasuga.registration.minecraft.common;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class DeferredFillHolder<R> implements Holder<R> {
    Holder<R> originalHolder;

    public void setValue(Holder<R> originalHolder) {
        if(this.originalHolder != null) {
            throw new IllegalStateException("Value already set");
        }
        this.originalHolder = originalHolder;
    }

    protected void checkInitialized() {
        if(this.originalHolder == null) {
            throw new IllegalStateException("Value not yet set");
        }
    }

    @Override
    public @NotNull R value() {
        checkInitialized();
        return originalHolder.value();
    }

    @Override
    public boolean isBound() {
        checkInitialized();
        return originalHolder.isBound();
    }

    @Override
    public boolean is(@NotNull ResourceLocation resourceLocation) {
        checkInitialized();
        return originalHolder.is(resourceLocation);
    }

    @Override
    public boolean is(@NotNull ResourceKey<R> resourceKey) {
        checkInitialized();
        return originalHolder.is(resourceKey);
    }

    @Override
    public boolean is(@NotNull Predicate<ResourceKey<R>> predicate) {
        checkInitialized();
        return originalHolder.is(predicate);
    }

    @Override
    public boolean is(@NotNull TagKey<R> tagKey) {
        checkInitialized();
        return originalHolder.is(tagKey);
    }

    @Override
    public boolean is(@NotNull Holder<R> holder) {
        checkInitialized();
        return false;
    }

    @Override
    public @NotNull Stream<TagKey<R>> tags() {
        checkInitialized();
        return originalHolder.tags();
    }

    @Override
    public @NotNull Either<ResourceKey<R>, R> unwrap() {
        checkInitialized();
        return originalHolder.unwrap();
    }

    @Override
    public @NotNull Optional<ResourceKey<R>> unwrapKey() {
        checkInitialized();
        return originalHolder.unwrapKey();
    }

    @Override
    public @NotNull Kind kind() {
        checkInitialized();
        return originalHolder.kind();
    }

    @Override
    public boolean canSerializeIn(@NotNull HolderOwner<R> holderOwner) {
        checkInitialized();
        return originalHolder.canSerializeIn(holderOwner);
    }
}
