package lib.kasuga.registration.minecraft.common;

import lib.kasuga.registration.Reg;
import lib.kasuga.registration.core.RegisterContext;
import lib.kasuga.registration.core.ResourceLocationModifiers;
import lib.kasuga.registration.stages.RegistrationStage;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

public abstract class MinecraftDeferRegistryReg<S extends Reg<S, T>, R, T extends R> extends Reg<S, T> {

    private final String name;
    private final ResourceKey<Registry<R>> entryKey;
    protected T value;

    protected Holder<R> holder = new DeferredFillHolder<>();

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
}
