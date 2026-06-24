package lib.kasuga.registration.data_driven.handler;

import com.google.gson.JsonObject;
import lib.kasuga.registration.Reg;
import lib.kasuga.registration.core.Modifier;
import lib.kasuga.registration.data_driven.property.JsonPropertyParser;
import lib.kasuga.registration.factory.FactoryRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.List;

public class BlockTypeHandler extends RegTypeHandler<BlockDef> {

    @Override
    public String getTypeName() { return "blocks"; }

    @Override
    public int getPhase() { return 1; }

    @Override
    public BlockDef parse(JsonObject json) {
        return new BlockDef(
            json.get("id").getAsString(),
            json.get("type").getAsString(),
            json.has("registry_group") ? json.get("registry_group").getAsString() : null,
            json.has("properties") ? json.getAsJsonObject("properties") : null,
            json.has("item_properties") ? json.getAsJsonObject("item_properties") : null
        );
    }

    @Override
    protected String resolveRawId(BlockDef definition) {
        return definition.id();
    }

    @Override
    protected String resolveRegistryGroup(BlockDef definition) {
        return definition.registryGroup();
    }

    @Override
    protected ResourceLocation resolveCreativeTab(BlockDef definition) {
        if (definition.itemProperties() != null && definition.itemProperties().has("tab")) {
            return ResourceLocation.parse(definition.itemProperties().get("tab").getAsString());
        }
        return null;
    }

    @Override
    protected Reg<?, ?> createRegistration(BlockDef definition, String path) {
        if (!FactoryRegistry.contains(definition.type())) return null;
        FactoryRegistry.BlockFactory factory = FactoryRegistry.get(definition.type());
        return factory.create(path);
    }

    @Override
    protected void configureTypeSpecific(BlockDef definition, Reg<?, ?> reg) {
        if (definition.properties() != null) {
            List<Modifier<BlockBehaviour.Properties>> mods =
                JsonPropertyParser.getInstance().parseBlockProperties(definition.properties());
            for (Modifier<BlockBehaviour.Properties> m : mods) {
                reg.configure(m);
            }
        }
    }
}
