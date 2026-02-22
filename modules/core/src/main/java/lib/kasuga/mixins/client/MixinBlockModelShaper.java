package lib.kasuga.mixins.client;

import lib.kasuga.content.block.UnModeledBlockProperty;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;

@Mixin(BlockModelShaper.class)
public abstract class MixinBlockModelShaper {

    @Unique
    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String kasugaLib$getValue(Property<T> pProperty, Comparable<?> pValue) {
        return pProperty.getName((T)pValue);
    }

    /**
     * @author MegumiKasuga
     * @reason To make the {@link UnModeledBlockProperty} usable.
     * That class works for special properties which wouldn't show in block states.
     */
    @Overwrite
    public static String statePropertiesToString(Map<Property<?>, Comparable<?>> pPropertyValues) {
        StringBuilder stringbuilder = new StringBuilder();

        for(Map.Entry<Property<?>, Comparable<?>> entry : pPropertyValues.entrySet()) {
            Property<?> property = entry.getKey();

            if (property instanceof UnModeledBlockProperty<?,?>) continue;

            if (!stringbuilder.isEmpty()) {
                stringbuilder.append(',');
            }

            stringbuilder.append(property.getName());
            stringbuilder.append('=');
            stringbuilder.append(kasugaLib$getValue(property, entry.getValue()));
        }

        return stringbuilder.toString();
    }

}
