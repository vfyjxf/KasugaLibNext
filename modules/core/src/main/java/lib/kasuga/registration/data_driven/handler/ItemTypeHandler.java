package lib.kasuga.registration.data_driven.handler;

import com.google.gson.JsonObject;
import lib.kasuga.registration.Reg;
import lib.kasuga.registration.core.Modifier;
import lib.kasuga.registration.data_driven.JsonItemParser;
import lib.kasuga.registration.data_driven.RegTypeHandler;
import lib.kasuga.registration.factory.FactoryRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.List;

public class ItemTypeHandler extends RegTypeHandler<ItemDef> {

    @Override
    public String getTypeName() { return "items"; }

    @Override
    public int getPhase() { return 1; }

    @Override
    public ItemDef parse(JsonObject json) {
        return new ItemDef(
            json.get("id").getAsString(),
            json.get("type").getAsString(),
            json.has("registry_group") ? json.get("registry_group").getAsString() : null,
            json.has("properties") ? json.getAsJsonObject("properties") : null
        );
    }

    @Override
    protected String resolveRawId(ItemDef definition) {
        return definition.id();
    }

    @Override
    protected String resolveRegistryGroup(ItemDef definition) {
        return definition.registryGroup();
    }

    @Override
    protected ResourceLocation resolveCreativeTab(ItemDef definition) {
        if (definition.properties() != null && definition.properties().has("tab")) {
            return ResourceLocation.parse(definition.properties().get("tab").getAsString());
        }
        return null;
    }

    @Override
    protected Reg<?, ?> createRegistration(ItemDef definition, String path) {
        if (!FactoryRegistry.containsItem(definition.type())) return null;
        FactoryRegistry.ItemFactory factory = FactoryRegistry.getItemFactory(definition.type());
        return factory.create(path);
    }

    @Override
    protected void configureTypeSpecific(ItemDef definition, Reg<?, ?> reg) {
        if (definition.properties() != null) {
            List<Modifier<Item.Properties>> mods =
                JsonItemParser.INSTANCE.parseItemProperties(definition.properties());
            for (Modifier<Item.Properties> m : mods) {
                reg.configure(m);
            }
        }
    }
}
