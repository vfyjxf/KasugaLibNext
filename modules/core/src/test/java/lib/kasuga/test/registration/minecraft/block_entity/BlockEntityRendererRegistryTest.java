package lib.kasuga.test.registration.minecraft.block_entity;

import lib.kasuga.KasugaLib;
import lib.kasuga.KasugaLibRegistry;
import lib.kasuga.registration.Registry;
import lib.kasuga.registration.beans.rendering.RenderingRegistry;
import lib.kasuga.registration.minecraft_old.block_entity.renderer.BlockEntityRendererReg;
import net.minecraft.server.MinecraftServer;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(EphemeralTestServerProvider.class)
public class BlockEntityRendererRegistryTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockEntityRendererRegistryTest.class);
    
    public static Registry registry = KasugaLibRegistry.getRegistryOf(KasugaLib.MODID);
    
    // 为测试方块实体注册渲染器
    public static BlockEntityRendererReg<TestCustomBlockEntity> TEST_BLOCK_ENTITY_RENDERER =
            new BlockEntityRendererReg<>(() -> new TestBlockEntityRenderer(null))
            .withBlockEntity(BlockEntityRegistryTest.TEST_CUSTOM_BLOCK_ENTITY)
            .setParent(registry);

    @Test
    public void testBlockEntityRendererRegistry(MinecraftServer server) {
        LOGGER.info("Testing Block Entity Renderer Registry on server side");
        
        // 在服务端，渲染器注册应该被忽略但记录日志
        try {
            // 验证渲染器注册对象不为空
            assert TEST_BLOCK_ENTITY_RENDERER != null;
            
            // 获取RenderingRegistry bean
            RenderingRegistry renderingRegistry = KasugaLib.getContext().getBean(RenderingRegistry.class);
            assert renderingRegistry != null;
            
            // 在服务端，这应该是DefaultRenderingRegistry实例，会输出日志但不实际注册
            LOGGER.info("RenderingRegistry type: {}", renderingRegistry.getClass().getSimpleName());
            
            // 手动触发注册以验证日志输出
            renderingRegistry.registerBlockEntityRenderer(
                BlockEntityRegistryTest.TEST_CUSTOM_BLOCK_ENTITY.getEntry(),
                () -> new TestBlockEntityRenderer(null)
            );
            
            LOGGER.info("Block entity renderer registration test completed - should see debug logs for ignored registrations");
            
        } catch (Exception e) {
            LOGGER.error("Error during block entity renderer registration test", e);
            throw e;
        }
    }
}
