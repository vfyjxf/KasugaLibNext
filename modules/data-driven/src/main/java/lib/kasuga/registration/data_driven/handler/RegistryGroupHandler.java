package lib.kasuga.registration.data_driven.handler;

import com.google.gson.JsonObject;
import lib.kasuga.registration.core.Modifier;
import lib.kasuga.registration.data_driven.BuildContext;
import lib.kasuga.registration.data_driven.JsonPropertyParser;
import lib.kasuga.registration.data_driven.JsonRegistryGroup;
import lib.kasuga.registration.data_driven.MetaTypeHandler;
import lib.kasuga.registration.data_driven.RegBuildContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.List;

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

    private void store(RegistryGroupDef definition, RegBuildContext context) {
        JsonRegistryGroup group = new JsonRegistryGroup(definition.id());

        if (definition.parent() != null) {
            JsonRegistryGroup parent = context.getRegistryGroup(definition.parent());
            group.setParent(parent != null ? parent : context.getRootGroup());
        } else {
            group.setParent(context.getRootGroup());
        }

        if (definition.properties() != null) {
            List<Modifier<BlockBehaviour.Properties>> mods =
                JsonPropertyParser.getInstance().parseBlockProperties(definition.properties());
            for (Modifier<BlockBehaviour.Properties> m : mods) {
                group.configure(m);
            }
        }

        if (definition.itemProperties() != null && definition.itemProperties().has("tab")) {
            String tabStr = definition.itemProperties().get("tab").getAsString();
            context.setRegistryGroupCreativeTab(definition.id(), ResourceLocation.parse(tabStr));
        }

        context.putRegistryGroup(definition.id(), group);
    }
}
