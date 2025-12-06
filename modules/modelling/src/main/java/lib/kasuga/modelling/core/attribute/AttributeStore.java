package lib.kasuga.modelling.core.attribute;

import lib.kasuga.modelling.core.element.Element;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class AttributeStore {
    private final Element element;
    Map<AttributeType<?>, Object> attributes = new HashMap<>();
    Map<ResourceLocation, Object> anonymousAttributes = new HashMap<>();

    public AttributeStore(Element element) {
        this.element = element;
    }

    public <V> void setAttribute(AttributeType<V> type, V attribute) {
        attributes.remove(type);
        ResourceLocation attributeLocation = Attributes.REGISTRY.getKey(type);
        if(attributeLocation != null) {
            anonymousAttributes.remove(attributeLocation);
        }
        attributes.put(type, attribute);
        type.onAttributeUpdate(element, attribute);
    }

    public void removeAttribute(AttributeType<?> type) {
        attributes.remove(type);
        ResourceLocation attributeLocation = Attributes.REGISTRY.getKey(type);
        if(attributeLocation != null) {
            anonymousAttributes.remove(attributeLocation);
        }
        type.onAttributeUpdate(element, null);
    }

    public <T> void setAttribute(ResourceLocation location, T attribute) {
        AttributeType<?> attributeType = Attributes.REGISTRY.get(location);
        anonymousAttributes.remove(location);
        if(attributeType == null || !attributeType.canCast(attribute)) {
            anonymousAttributes.put(location, attribute);
            return;
        }
        //noinspection unchecked
        AttributeType<T> attributeTypeCasted = (AttributeType<T>) attributeType;
        T attributeCasted = attributeTypeCasted.cast(attribute);
        setAttribute(attributeTypeCasted, attributeCasted);
    }

    @SuppressWarnings("unchecked")
    public <V> V getAttribute(AttributeType<V> type) {
        return (V) attributes.get(type);
    }

    public Object getAttribute(ResourceLocation location) {
        AttributeType<?> attributeType = Attributes.REGISTRY.get(location);
        if(attributeType != null) {
            Object attribute = attributes.get(attributeType);
            if(attribute != null) {
                return attribute;
            }
        }
        return anonymousAttributes.get(location);
    }

    public void removeAttribute(ResourceLocation location) {
        AttributeType<?> attributeType = Attributes.REGISTRY.get(location);
        if(attributeType != null) {
            removeAttribute(attributeType);
        } else {
            anonymousAttributes.remove(location);
        }
    }
}
