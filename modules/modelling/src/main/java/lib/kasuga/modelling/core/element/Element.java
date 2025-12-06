package lib.kasuga.modelling.core.element;

import lib.kasuga.modelling.core.attribute.AttributeStore;
import lib.kasuga.modelling.core.attribute.AttributeType;
import lib.kasuga.modelling.core.event.Event;
import lib.kasuga.modelling.core.event.EventEmitter;
import lib.kasuga.modelling.core.event.EventTarget;
import lib.kasuga.modelling.core.event.EventType;
import lib.kasuga.modelling.core.style.StyleStore;
import lib.kasuga.modelling.core.style.StyleType;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public abstract class Element implements EventTarget {
    @Getter @Nullable
    protected Element parent;
    protected List<Element> children;
    @Getter protected final AttributeStore attributes = new AttributeStore(this);
    @Getter protected final StyleStore styleStore = new StyleStore(this);
    @Getter protected final EventEmitter emitter = new EventEmitter();
    @Getter protected final ElementRenderer renderer = createModelRenderer();
    protected abstract ElementRenderer createModelRenderer();

    public void setParent(@Nullable Element parent) {
        this.parent = parent;
        this.styleStore.setParent(parent != null ? parent.styleStore : null);
    }

    protected static void removeChildFromParent(Element element) {
        if(element.parent != null) {
            element.setParent(null);
        }
    }

    public int addChild(Element element) {
        removeChildFromParent(element);
        int size = children.size();
        children.add(element);
        element.setParent(this);
        this.renderer.onChildAdded(size, element);
        return size;
    }

    public int removeChild(Element element) {
        int index = children.indexOf(element);
        children.remove(element);
        element.setParent(null);
        this.renderer.onChildRemoved(index, element);
        return index;
    }

    public int removeChildAt(int index) {
        if(index < 0 || index > this.children.size())
            return -1;
        return removeChild(this.children.get(index));
    }

    public int addChildBefore(Element source, Element element) {
        removeChildFromParent(element);
        int index = children.indexOf(source);
        if(index < 0)
            index = 0;
        children.add(index, element);
        element.setParent(this);
        this.renderer.onChildAdded(index, element);
        return index;
    }

    public int addChildAfter(Element source, Element element) {
        removeChildFromParent(element);
        int index = children.indexOf(source) + 1;
        if(index > children.size())
            index = children.size();
        children.add(index, element);
        element.setParent(this);
        this.renderer.onChildAdded(index, element);
        return index;
    }

    public <T> T getAttribute(AttributeType<T> type) {
        return this.attributes.getAttribute(type);
    }

    public Object getAttribute(ResourceLocation location) {
        return this.attributes.getAttribute(location);
    }

    public <T> void setAttribute(AttributeType<T> type, T attribute) {
        this.attributes.setAttribute(type, attribute);
    }

    public <T> void setAttribute(ResourceLocation location, T attribute) {
        this.attributes.setAttribute(location, attribute);
    }

    public void removeAttribute(AttributeType<?> type) {
        this.attributes.removeAttribute(type);
    }

    public void removeAttribute(ResourceLocation location) {
        this.attributes.removeAttribute(location);
    }

    public void dispatchEvent(Event event) {
        dispatchEvent(event, false);
    }

    public void dispatchEvent(Event event, boolean capture) {}

    @Override
    public <T extends Event> void addEventListener(EventType<T> type, Consumer<T> consumer, boolean capture) {
        emitter.addEventListener(type, consumer, capture);
    }

    @Override
    public void addEventListener(String name, Consumer<Event> consumer, boolean capture) {
        emitter.addEventListener(name, consumer, capture);
    }

    @Override
    public void removeEventListener(EventType<?> type, Consumer<?> consumer, boolean capture) {
        emitter.removeEventListener(type, consumer, capture);
    }

    @Override
    public void removeEventListener(String name, Consumer<Event> consumer, boolean capture) {
        emitter.removeEventListener(name, consumer, capture);
    }

    public void onStyleUpdated(StyleStore styleStore, Set<StyleType<?>> keys) {
        this.renderer.updateStyle(styleStore, keys);
    }
}
