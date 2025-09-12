package lib.kasuga.test.registration.minecraft.entity;

import lib.kasuga.registration.minecraft.entity.renderer.EntityRendererBuilder;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class TestProjectileEntityRenderer extends EntityRenderer<TestProjectileEntity> implements EntityRendererBuilder<TestProjectileEntity> {
    
    public TestProjectileEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(TestProjectileEntity entity) {
        return ResourceLocation.withDefaultNamespace("textures/entity/arrow.png");
    }

    @Override
    public EntityRenderer<TestProjectileEntity> build(EntityRendererProvider.Context context) {
        return new TestProjectileEntityRenderer(context);
    }
}
