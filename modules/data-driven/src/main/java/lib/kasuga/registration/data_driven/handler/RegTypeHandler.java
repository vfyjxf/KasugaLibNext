package lib.kasuga.registration.data_driven.handler;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import lib.kasuga.registration.Reg;
import lib.kasuga.registration.core.ResourceLocationModifiers;
import lib.kasuga.registration.data_driven.TypeHandler;
import lib.kasuga.registration.data_driven.context.BuildContext;
import lib.kasuga.registration.data_driven.context.JsonRegistryGroup;
import lib.kasuga.registration.data_driven.context.RegBuildContext;
import lib.kasuga.registration.minecraft.item.ItemRegModifiers;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public abstract class RegTypeHandler<T> implements TypeHandler<T> {

    private static final Logger LOGGER = LogUtils.getLogger();

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
        try {
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
        } catch (Exception e) {
            LOGGER.warn("Failed to apply registration for '{}': {}",
                resolveRawId(definition), e.getMessage());
            LOGGER.debug("Full stacktrace:", e);
        }
    }
}
