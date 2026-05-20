package lib.kasuga.registration.data_driven;

import com.mojang.logging.LogUtils;
import io.micronaut.context.annotation.Context;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import lib.kasuga.registration.Reg;
import lib.kasuga.registration.core.RegisterContextRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Context
public class CreativeTabCollector {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<Reg<?, ?>, ResourceLocation> JSON_TABS = new ConcurrentHashMap<>();

    @Inject
    private RegisterContextRegistry registry;

    public static void register(Reg<?, ?> reg, ResourceLocation tabId) {
        JSON_TABS.put(reg, tabId);
    }

    @PostConstruct
    public void init() {
        registry.register(RegisterContextRegistry.Side.COMMON, (modRegistry, modEventBus) ->
            modEventBus.addListener(BuildCreativeModeTabContentsEvent.class, this::fillTab)
        );
    }

    private static Block findBlock(Reg<?, ?> reg) {
        Object entry = reg.getEntry();
        if (entry instanceof Block block) return block;
        // Wrapper regs (e.g. SlabReg) return null; check children for BlockReg
        for (Reg<?, ?> child : reg.getChildren()) {
            Block result = findBlock(child);
            if (result != null) return result;
        }
        return null;
    }

    private void fillTab(BuildCreativeModeTabContentsEvent event) {
        ResourceLocation currentTab = event.getTabKey().location();
        int matched = 0;
        for (Map.Entry<Reg<?, ?>, ResourceLocation> entry : JSON_TABS.entrySet()) {
            if (!entry.getValue().equals(currentTab)) continue;
            matched++;

            Reg<?, ?> reg = entry.getKey();
            try {
                Block block = findBlock(reg);
                if (block != null) {
                    event.accept(block.asItem().getDefaultInstance());
                } else {
                    LOGGER.warn("CreativeTabCollector: findBlock returned null for {}", reg);
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to add item to creative tab", e);
            }
        }
    }
}
