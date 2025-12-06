package lib.kasuga.modelling.core.style;

import lib.kasuga.modelling.core.element.Element;

public interface StyleType<T> {
    public default void onApply(Element element, StyleMap otherMap, T newValue) {}
}
