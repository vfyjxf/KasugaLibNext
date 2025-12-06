package lib.kasuga.modelling.core.element;

import lib.kasuga.modelling.core.style.StyleStore;
import lib.kasuga.modelling.core.style.StyleType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class ElementRenderer {

    protected List<ElementRenderer> renderers = new ArrayList<>();

    public void onChildAdded(int index, Element element) {
        renderers.add(index, element.getRenderer());
    }

    public void onChildRemoved(int index, Element element) {
        renderers.remove(index);
    }

    public void updateStyle(StyleStore styleStore, Set<StyleType<?>> keys) {}
}
