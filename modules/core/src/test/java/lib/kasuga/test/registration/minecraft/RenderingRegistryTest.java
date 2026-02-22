package lib.kasuga.test.registration.minecraft;

import lib.kasuga.KasugaLib;
import lib.kasuga.KasugaLibRegistry;
import lib.kasuga.registration.Registry;
import lib.kasuga.registration.beans.rendering.DefaultRenderingRegistry;
import lib.kasuga.registration.beans.rendering.RenderingRegistry;
import lib.kasuga.registration.minecraft.block_entity.BlockEntityRendererReg;
import lib.kasuga.registration.minecraft.entity.EntityRendererReg;
import lib.kasuga.test.registration.minecraft.block_entity.BlockEntityRegistryTest;
import lib.kasuga.test.registration.minecraft.block_entity.TestBlockEntityRenderer;
import lib.kasuga.test.registration.minecraft.block_entity.TestCustomBlockEntity;
import lib.kasuga.test.registration.minecraft.entity.*;
import net.minecraft.server.MinecraftServer;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(EphemeralTestServerProvider.class)
public class RenderingRegistryTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RenderingRegistryTest.class);
    
    public static Registry registry = KasugaLibRegistry.getRegistryOf(KasugaLib.MODID);
    
    // 实体渲染器注册
    public static EntityRendererReg<TestEntity> TEST_ENTITY_RENDERER =
            new EntityRendererReg<>(() -> TestEntityRenderer::new)
            .withEntity(EntityRegistryTest.TEST_CREATURE_ENTITY::getEntry)
            .setParent(registry);
    
    public static EntityRendererReg<TestEntity> TEST_PROJECTILE_RENDERER =
            new EntityRendererReg<>(() -> TestEntityRenderer::new)
            .withEntity(EntityRegistryTest.TEST_PROJECTILE_ENTITY::getEntry)
            .setParent(registry);
    
    // 方块实体渲染器注册
    public static BlockEntityRendererReg<TestCustomBlockEntity> TEST_BLOCK_ENTITY_RENDERER =
            new BlockEntityRendererReg<>(() -> new TestBlockEntityRenderer(null))
            .withBlockEntity(BlockEntityRegistryTest.TEST_CUSTOM_BLOCK_ENTITY::getEntry)
            .setParent(registry);

    @Test
    public void testRenderingRegistry(MinecraftServer server) {
        LOGGER.info("========== Testing Rendering Registry on Server Side ==========");
        
        try {
            // 验证所有渲染器注册对象不为空
            assert TEST_ENTITY_RENDERER != null;
            assert TEST_PROJECTILE_RENDERER != null;
            assert TEST_BLOCK_ENTITY_RENDERER != null;
            
            LOGGER.info("All renderer registration objects created successfully");
            
            // 获取RenderingRegistry bean
            RenderingRegistry renderingRegistry = KasugaLib.getBean(RenderingRegistry.class);
            assert renderingRegistry != null;
            
            // 在服务端，这应该是DefaultRenderingRegistry实例
            LOGGER.info("RenderingRegistry implementation: {}", renderingRegistry.getClass().getSimpleName());
            
            // 测试实体渲染器注册（服务端应该记录调试日志）
            LOGGER.info("Testing Entity Renderer Registration:");

            assert renderingRegistry instanceof DefaultRenderingRegistry;
            
            LOGGER.info("========== Rendering Registry Test Completed ==========");
            LOGGER.info("Expected: Debug logs showing ignored server-side registrations");
            LOGGER.info("In client environment, these would register actual renderers");
            
        } catch (Exception e) {
            LOGGER.error("Error during rendering registry test", e);
            throw e;
        }
    }
}
