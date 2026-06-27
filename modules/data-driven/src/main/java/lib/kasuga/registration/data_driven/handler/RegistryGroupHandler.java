package lib.kasuga.registration.data_driven.handler;

import com.google.gson.JsonObject;
import lib.kasuga.registration.Reg;
import lib.kasuga.registration.data_driven.context.BuildContext;
import lib.kasuga.registration.data_driven.context.JsonRegistryGroup;
import lib.kasuga.registration.data_driven.context.RegBuildContext;
import lib.kasuga.registration.data_driven.property.JsonPropertyParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.List;
import java.util.function.Consumer;

public class RegistryGroupHandler extends MetaTypeHandler<RegistryGroupDef> {

    @Override
    public String getTypeName() { return "registry_groups"; }

    @Override
    public int getPhase() { return 0; }

    @Override
    public RegistryGroupDef parse(JsonObject json) {
        return new RegistryGroupDef(
            json.get("id").getAsString(),
            json.has("parent") ? json.get("parent").getAsString() : null,
            json.has("properties") ? json.getAsJsonObject("properties") : null,
            json.has("item_properties") ? json.getAsJsonObject("item_properties") : null
        );
    }

    @Override
    public void apply(RegistryGroupDef definition, BuildContext context) {
        store(definition, (RegBuildContext) context);
    }

    @SuppressWarnings("unchecked")
    private void store(RegistryGroupDef definition, RegBuildContext context) {
        JsonRegistryGroup group = new JsonRegistryGroup(definition.id());

        if (definition.parent() != null) {
            JsonRegistryGroup parent = context.getRegistryGroup(definition.parent());
            group.setParent(parent != null ? parent : context.getRootGroup());
        } else {
            group.setParent(context.getRootGroup());
        }

        if (definition.properties() != null) {
            List<Consumer<BlockBehaviour.Properties>> mods =
                JsonPropertyParser.getInstance().parseBlockProperties(definition.properties());
            for (Consumer<BlockBehaviour.Properties> m : mods) {
                group.withProperty(BlockBehaviour.Properties.class, m);
            }
        }

        if (definition.itemProperties() != null && definition.itemProperties().has("tab")) {
            String tabStr = definition.itemProperties().get("tab").getAsString();
            context.setRegistryGroupCreativeTab(definition.id(), ResourceLocation.parse(tabStr));
        }

        context.putRegistryGroup(definition.id(), group);
    }
}
