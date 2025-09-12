package lib.kasuga.test.registration.minecraft.entity;

import lib.kasuga.KasugaLib;
import lib.kasuga.KasugaLibRegistry;
import lib.kasuga.registration.Registry;
import lib.kasuga.registration.beans.rendering.RenderingRegistry;
import lib.kasuga.registration.minecraft.entity.renderer.EntityRendererReg;
import net.minecraft.server.MinecraftServer;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(EphemeralTestServerProvider.class)
public class EntityRendererRegistryTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityRendererRegistryTest.class);
    
    public static Registry registry = KasugaLibRegistry.getRegistryOf(KasugaLib.MODID);
    
    // 为测试实体注册渲染器
    public static EntityRendererReg<TestEntity> TEST_ENTITY_RENDERER = 
            new EntityRendererReg<>(() -> new TestEntityRenderer(null))
            .withEntity(EntityRegistryTest.TEST_CREATURE_ENTITY)
            .setParent(registry);
    
    // 为投射物实体注册渲染器  
    public static EntityRendererReg<TestProjectileEntity> TEST_PROJECTILE_RENDERER = 
            new EntityRendererReg<>(() -> new TestProjectileEntityRenderer(null))
            .withEntity(EntityRegistryTest.TEST_PROJECTILE_ENTITY)
            .setParent(registry);

    @Test
    public void testEntityRendererRegistry(MinecraftServer server) {
        LOGGER.info("Testing Entity Renderer Registry on server side");
        
        // 在服务端，渲染器注册应该被忽略但记录日志
        try {
            // 验证渲染器注册对象不为空
            assert TEST_ENTITY_RENDERER != null;
            assert TEST_PROJECTILE_RENDERER != null;
            
            // 获取RenderingRegistry bean
            RenderingRegistry renderingRegistry = KasugaLib.getContext().getBean(RenderingRegistry.class);
            assert renderingRegistry != null;
            
            // 在服务端，这应该是DefaultRenderingRegistry实例，会输出日志但不实际注册
            LOGGER.info("RenderingRegistry type: {}", renderingRegistry.getClass().getSimpleName());
            
            // 手动触发注册以验证日志输出
            renderingRegistry.registerEntityRenderer(
                EntityRegistryTest.TEST_CREATURE_ENTITY.getEntry(), 
                () -> new TestEntityRenderer(null)
            );
            
            renderingRegistry.registerEntityRenderer(
                EntityRegistryTest.TEST_PROJECTILE_ENTITY.getEntry(),
                () -> new TestProjectileEntityRenderer(null)
            );
            
            LOGGER.info("Entity renderer registration test completed - should see debug logs for ignored registrations");
            
        } catch (Exception e) {
            LOGGER.error("Error during entity renderer registration test", e);
            throw e;
        }
    }
}
