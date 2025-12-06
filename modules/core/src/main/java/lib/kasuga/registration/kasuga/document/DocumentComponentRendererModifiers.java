package lib.kasuga.registration.kasuga.document;

import lib.kasuga.content.document.DocumentComponentType;
import lib.kasuga.registration.core.Modifier;
import lib.kasuga.registration.core.ModifierType;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

public class DocumentComponentRendererModifiers {
    public static final ModifierType<Collection<DocumentComponentType<?>>> DOCUMENT_COMPONENTS = new ModifierType<>();
    public static class DocumentComponentTypeListModifier extends Modifier<Collection<DocumentComponentType<?>>> {
        @Override
        public ModifierType<Collection<DocumentComponentType<?>>> getType() {
            return DOCUMENT_COMPONENTS;
        }
    }

    public static final Function<Supplier<Collection<DocumentComponentType<?>>>, Modifier<Collection<DocumentComponentType<?>>>>
            DOCUMENT_COMPONENTS_BY_SUPPLIER = Util.memoize((supplier) -> new DocumentComponentTypeListModifier() {
        @Override
        public Collection<DocumentComponentType<?>> transform(Collection<DocumentComponentType<?>> originalValue) {
            originalValue.addAll(supplier.get());
            return originalValue;
        }
    });
}
