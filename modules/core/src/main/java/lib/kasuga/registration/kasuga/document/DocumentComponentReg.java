package lib.kasuga.registration.kasuga.document;

import lib.kasuga.content.document.DocumentComponentRegistries;
import lib.kasuga.content.document.DocumentComponentType;
import lib.kasuga.registration.minecraft.common.MinecraftDeferRegistryReg;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class DocumentComponentReg<T extends DocumentComponentType<?>> extends MinecraftDeferRegistryReg<DocumentComponentReg<T>, DocumentComponentType<?>, T> {

    private final Supplier<T> supplier;

    public DocumentComponentReg(String name, Supplier<T> supplier) {
        super(name, DocumentComponentRegistries.DOCUMENT_COMPONENT_KEY);
        this.supplier = supplier;
    }

    @Override
    protected T createObject(ResourceLocation id) {
        return supplier.get();
    }
}
