package lib.kasuga.modelling.core.style;

import lib.kasuga.modelling.core.element.Element;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class StyleStore implements StyleMap.Writable {
    @Getter @Nullable
    StyleStore parent;
    protected StyleMap.Writable importantLocalStyle = new WritableHashMap();
    protected StyleMap.Writable localStyle = new WritableHashMap();
    protected StyleMap.Writable computedStyle = new WritableHashMap();
    private final Element element;
    protected final List<StyleStore> children = new ArrayList<>();
    @Getter
    protected Stylesheet.Subscribable stylesheet;

    public StyleStore(Element element) {
        this.element = element;
    }


    @Override
    public Set<StyleType<?>> keySet() {
        return this.localStyle.keySet();
    }

    @Override
    public boolean containsKey(StyleType<?> key) {
        return this.localStyle.containsKey(key);
    }

    @Override
    public <T> T get(StyleType<T> key) {
        return this.localStyle.get(key);
    }

    @Override
    public int size() {
        return this.localStyle.size();
    }

    @Override
    public <T> boolean put(StyleType<T> key, T value) {
        return put(key, value, false);
    }

    public <T> boolean put(StyleType<T> key, T value, boolean important) {
        boolean res;
        if(important) {
            this.importantLocalStyle.put(key, value);
        }
        res = this.localStyle.put(key, value);
        this.recompute();
        return res;
    }

    @Override
    public void putAll(StyleMap otherMap) {
        this.localStyle.putAll(otherMap);
        this.recompute();
    }

    @Override
    public Set<StyleType<?>> apply(StyleMap otherMap, Element element) {
        throw new UnsupportedOperationException("Cannot use apply on a style storage");
    }

    @Override
    public <T> T remove(StyleType<T> key) {
        T val = this.localStyle.remove(key);
        this.importantLocalStyle.remove(key);
        this.recompute();
        return val;
    }

    @Override
    public void clear() {
        this.localStyle.clear();
        this.importantLocalStyle.clear();
        this.recompute();
    }

    public void setParent(@Nullable StyleStore parent) {
        if(this.parent != null) {
            this.parent.children.remove(this);
            this.parent = null;
        }
        if(parent != null) {
            this.parent = parent;
            this.parent.children.add(this);
        }
        this.recompute();
    }

    public void setStylesheet(Stylesheet.Subscribable stylesheet) {
        if(this.stylesheet != null){
            this.stylesheet.removeAssociation(this);
            this.stylesheet = null;
        }
        if(stylesheet != null) {
            this.stylesheet = stylesheet;
            stylesheet.addAssociation(this);
        }
        this.recompute();
    }

    protected void recompute() {
        Writable finalStyle = new WritableHashMap();
        // extend from parent
        if(parent != null)
            finalStyle.putAll(parent.computedStyle);

        Writable computedWithStylesheet = new WritableHashMap();

        if(this.stylesheet != null)
            this.stylesheet.apply(this.element, computedWithStylesheet);

        computedWithStylesheet.putAll(this.localStyle);

        ArrayList<StyleType<?>> list = new ArrayList<>(this.localStyle.size());

        for (StyleType<?> styleType : computedWithStylesheet.keySet()) {
            if(styleType instanceof CompositeStyle<?> composite) {
                //noinspection unchecked
                CompositeStyle<? super Object> casted = (CompositeStyle<? super Object>)composite;
                Object value = this.localStyle.get(styleType);
                finalStyle.putAll(casted.getEquivalentStyles(value));
            } else list.add(styleType);
        }

        for (StyleType<?> styleType : list) {
            Object value = this.localStyle.get(styleType);
            //noinspection unchecked
            finalStyle.put((StyleType<? super Object>) styleType, value);
        }


        computedWithStylesheet.clear();

        list.clear();

        // important styles override all
        stylesheet.applyImportant(this.element, computedWithStylesheet);

        for (StyleType<?> styleType : this.importantLocalStyle.keySet()) {
            if(styleType instanceof CompositeStyle<?> composite) {
                //noinspection unchecked
                CompositeStyle<? super Object> casted = (CompositeStyle<? super Object>)composite;
                Object value = this.importantLocalStyle.get(styleType);
                finalStyle.putAll(casted.getEquivalentStyles(value));
            } else list.add(styleType);
        }

        Set<StyleType<?>> keys =  this.computedStyle.apply(finalStyle, this.element);

        this.element.onStyleUpdated(this, keys);

        for (StyleStore child : this.children) {
            child.recompute();
        }
    }


    public <T> T getComputedStyle(StyleType<T> type) {
        return computedStyle.get(type);
    }
}
