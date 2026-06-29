package lib.kasuga.registration.minecraft.common;

import com.mojang.logging.LogUtils;
import lib.kasuga.registration.Reg;
import lib.kasuga.registration.core.RegisterContext;
import lib.kasuga.registration.core.ResourceLocationModifiers;
import lib.kasuga.registration.stages.RegistrationStage;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slf4j.Logger;

public abstract class MinecraftDeferRegistryReg<S extends Reg<S, T>, R, T extends R> extends Reg<S, T> {
    private static Logger LOGGER = LogUtils.getLogger();
    private final String name;
    private final ResourceKey<Registry<R>> entryKey;
    protected T value;

    protected Holder<R> holder = new DeferredFillHolder<>();
    private ResourceLocation resourceLocation;

    protected MinecraftDeferRegistryReg(String name, ResourceKey<Registry<R>> entryKey) {
        this.name = name;
        this.entryKey = entryKey;
    }

    @Override
    public void register(RegisterContext<?> context) {
        context.onStage(RegistrationStage.REGISTER_EVENT, (ctx)-> {
            if (ctx.getRegistryKey() != entryKey) return;

            ResourceLocation id = transform(
                    ResourceLocationModifiers.ID,
                    ResourceLocation.fromNamespaceAndPath("minecraft", name)
            );
            id = applyProperties(ResourceLocation.class, id);

            this.resourceLocation = id;

            LOGGER.info("Posting deferred registry entry: {} to {}", id, entryKey.location());
            value = this.createObject(id);
            Holder<R> localHolder = DeferredHolder.create(entryKey, id);
            if(this.holder instanceof DeferredFillHolder<R> dfh) {
                dfh.setValue(localHolder);
            }
            this.holder = localHolder;
            ctx.register(entryKey, id, ()->value);
        });
    }

    protected abstract T createObject(ResourceLocation id);

    @Override
    public T getEntry() {
        if(this.value == null) {
            throw new IllegalStateException("Registry item not present:" + name);
        }
        return this.value;
    }

    public String getName() {
        return name;
    }

    public Holder<R> getHolder() {
        return holder;
    }

    public ResourceLocation getResourceLocation() {
        return resourceLocation;
    }
}
