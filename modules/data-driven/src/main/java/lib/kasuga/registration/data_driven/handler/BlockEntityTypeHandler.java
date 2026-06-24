package lib.kasuga.registration.data_driven.handler;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import lib.kasuga.registration.Reg;
import lib.kasuga.registration.data_driven.TypeHandler;
import lib.kasuga.registration.data_driven.context.BuildContext;
import lib.kasuga.registration.data_driven.context.RegBuildContext;
import lib.kasuga.registration.factory.FactoryRegistry;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;

import java.util.List;

public class BlockEntityTypeHandler implements TypeHandler<BlockEntityDef> {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public String getTypeName() { return "block_entities"; }

    @Override
    public int getPhase() { return 2; }

    @Override
    public String getParentTypeName() { return "blocks"; }

    @Override
    public List<JsonObject> extractEmbedded(JsonObject blockJson) {
        if (!blockJson.has("block_entity")) return null;
        JsonObject be = blockJson.getAsJsonObject("block_entity").deepCopy();
        be.addProperty("_parent_block", blockJson.get("id").getAsString());
        return List.of(be);
    }

    @Override
    public BlockEntityDef parse(JsonObject json) {
        return new BlockEntityDef(
            json.get("type").getAsString(),
            json.get("_parent_block").getAsString()
        );
    }

    @Override
    public void apply(BlockEntityDef definition, BuildContext baseContext) {
        RegBuildContext context = (RegBuildContext) baseContext;
        Reg<?, Block> blockReg = context.getBlockReg(definition.parentBlockId());
        if (blockReg == null) {
            LOGGER.warn("Block '{}' not found for block entity association", definition.parentBlockId());
            return;
        }

        if (!FactoryRegistry.containsBlockEntity(definition.beType())) {
            LOGGER.warn("Unknown block entity type '{}' for block '{}', registered types: {}",
                definition.beType(), definition.parentBlockId(), FactoryRegistry.getBlockEntityTypes());
            return;
        }

        String beName = definition.parentBlockId().split(":", 2).length > 1
            ? definition.parentBlockId().split(":", 2)[1] + "_be"
            : definition.parentBlockId() + "_be";

        FactoryRegistry.BlockEntityFactory factory = FactoryRegistry.getBlockEntityFactory(definition.beType());
        Reg<?, ?> beReg = factory.create(beName, () -> new Block[]{blockReg.getEntry()});
        blockReg.addChild(beReg);
        context.putReg("block_entities", definition.parentBlockId(), beReg);

        LOGGER.info("[BlockEntityHandler] BE '{}' attached to block '{}'", beName, definition.parentBlockId());
    }
}
