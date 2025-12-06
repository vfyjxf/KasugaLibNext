package lib.kasuga.modelling.core.attribute;

import lib.kasuga.modelling.core.element.Element;

public interface AttributeType<T> {
    public void onAttributeUpdate(Element element, T attribute);

    public boolean canCast(Object attribute);

    public T cast(Object attribute);
}
