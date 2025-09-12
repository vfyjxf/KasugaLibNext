package lib.kasuga.registration.minecraft.entity.renderer;

import lib.kasuga.KasugaLib;
import lib.kasuga.registration.Reg;
import lib.kasuga.registration.beans.rendering.RenderingRegistry;
import lib.kasuga.registration.core.RegisterContext;
import lib.kasuga.registration.stages.RegistrationStage;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Supplier;

public class EntityRendererReg<E extends Entity> extends Reg<EntityRendererReg<E>, Void> implements EntityRendererConfigurations<EntityRendererReg<E>> {

    private final Supplier<EntityRendererBuilder<E>> provider;

    public EntityRendererReg(Supplier<EntityRendererBuilder<E>> provider) {
        super();
        this.provider = provider;
    }

    @Override
    public void register(RegisterContext<?> context) {
        super.register(context);
        context.onStage(RegistrationStage.BAKING_COMPLETE, (ctx)->{
            Collection<EntityType<?>> validEntities = this.transform(EntityRendererModifiers.ENTITY_TYPES, new HashSet<>());
            for (EntityType<?> validEntity : validEntities) {
                //noinspection unchecked
                KasugaLib.getContext().getBean(RenderingRegistry.class)
                        .registerEntityRenderer((EntityType<E>) validEntity, provider);
            }
        });
    }

    @Override
    public Void getEntry() {
        throw new IllegalStateException("EntityRendererReg does not have an entry");
    }

    public void block() {}
}
