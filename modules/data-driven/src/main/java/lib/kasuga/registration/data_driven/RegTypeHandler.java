package lib.kasuga.registration.data_driven;

import com.google.gson.JsonObject;
import lib.kasuga.registration.Reg;
import lib.kasuga.registration.core.ResourceLocationModifiers;
import lib.kasuga.registration.minecraft.item.ItemRegModifiers;
import net.minecraft.resources.ResourceLocation;

public abstract class RegTypeHandler<T> implements TypeHandler<T> {

    protected abstract String resolveRawId(T definition);

    protected abstract Reg<?, ?> createRegistration(T definition, String path);

    protected void configureTypeSpecific(T definition, Reg<?, ?> reg) {}

    protected String resolveRegistryGroup(T definition) { return null; }

    protected JsonObject resolveItemProperties(T definition) { return null; }

    protected ResourceLocation resolveCreativeTab(T definition) { return null; }

    protected String resolveNamespace(String rawId) {
        String[] parts = rawId.split(":", 2);
        return parts.length > 1 ? parts[0] : "minecraft";
    }

    protected String resolvePath(String rawId) {
        String[] parts = rawId.split(":", 2);
        return parts.length > 1 ? parts[1] : parts[0];
    }

    @Override
    public void apply(T definition, BuildContext baseContext) {
        RegBuildContext context = (RegBuildContext) baseContext;
        String rawId = resolveRawId(definition);
        String path = resolvePath(rawId);
        String namespace = resolveNamespace(rawId);

        Reg<?, ?> reg = createRegistration(definition, path);
        if (reg == null) return;

        if (!namespace.equals("minecraft")) {
            reg.configure(ResourceLocationModifiers.withNamespace(namespace));
        }

        String registryGroupId = resolveRegistryGroup(definition);
        if (registryGroupId != null) {
            JsonRegistryGroup group = context.getRegistryGroup(registryGroupId);
            reg.setParent(group != null ? group : context.getRootGroup());
        } else {
            reg.setParent(context.getRootGroup());
        }

        ResourceLocation tab = resolveCreativeTab(definition);
        if (tab != null) {
            reg.configure(ItemRegModifiers.TAB_TO_BY_KEY_BY_SUPPLIER.apply(() -> tab));
        } else if (registryGroupId != null) {
            ResourceLocation groupTab = context.getRegistryGroupCreativeTab(registryGroupId);
            if (groupTab != null) {
                reg.configure(ItemRegModifiers.TAB_TO_BY_KEY_BY_SUPPLIER.apply(() -> groupTab));
            }
        }

        configureTypeSpecific(definition, reg);

        context.putReg(getTypeName(), rawId, reg);
    }
}
