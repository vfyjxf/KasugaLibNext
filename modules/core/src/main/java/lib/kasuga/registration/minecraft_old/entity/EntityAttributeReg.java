package lib.kasuga.registration.minecraft_old.entity;

import lib.kasuga.registration.Reg;
import lib.kasuga.registration.core.RegisterContext;
import lib.kasuga.registration.stages.RegistrationStage;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import java.util.function.Supplier;

/**
 * Registration for entity attributes.
 * This class handles the registration of entity attributes for living entities.
 * 
 * @param <T> the living entity type
 */
public final class EntityAttributeReg<T extends LivingEntity> extends Reg<EntityAttributeReg<T>, AttributeSupplier> {

    private final EntityReg<T> entityReg;
    private final Supplier<AttributeSupplier.Builder> attributeBuilder;
    private AttributeSupplier attributeSupplier;

    public static <T extends LivingEntity> EntityAttributeReg<T> of(EntityReg<T> entityReg, 
            Supplier<AttributeSupplier.Builder> attributeBuilder) {
        return new EntityAttributeReg<>(entityReg, attributeBuilder);
    }

    public EntityAttributeReg(EntityReg<T> entityReg, Supplier<AttributeSupplier.Builder> attributeBuilder) {
        this.entityReg = entityReg;
        this.attributeBuilder = attributeBuilder;
    }

    @Override
    public void register(RegisterContext<?> context) {
        context.onStage(RegistrationStage.COMMON_SETUP, (ctx) -> {
            EntityType<T> entityType = entityReg.getEntry();
            attributeSupplier = attributeBuilder.get().build();
            // Note: In newer versions, attribute registration might need to be handled differently
            // This is a placeholder for the proper attribute registration mechanism
        });
    }

    @Override
    public AttributeSupplier getEntry() {
        if (attributeSupplier == null) {
            throw new IllegalStateException("Entity attributes not yet created");
        }
        return attributeSupplier;
    }

    public EntityReg<T> getEntityReg() {
        return entityReg;
    }

    public AttributeSupplier.Builder getAttributeBuilder() {
        return attributeBuilder.get();
    }
}
