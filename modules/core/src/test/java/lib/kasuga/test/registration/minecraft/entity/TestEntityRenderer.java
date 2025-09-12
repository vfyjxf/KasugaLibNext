package lib.kasuga.test.registration.minecraft.entity;

import lib.kasuga.registration.minecraft.entity.renderer.EntityRendererBuilder;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class TestEntityRenderer extends EntityRenderer<TestEntity> implements EntityRendererBuilder<TestEntity> {
    
    public TestEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(TestEntity entity) {
        return ResourceLocation.withDefaultNamespace("textures/entity/chicken.png");
    }

    @Override
    public EntityRenderer<TestEntity> build(EntityRendererProvider.Context context) {
        return new TestEntityRenderer(context);
    }
}
