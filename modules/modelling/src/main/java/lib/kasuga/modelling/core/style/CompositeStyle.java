package lib.kasuga.modelling.core.style;

public interface CompositeStyle<T> extends StyleType<T> {
    public StyleMap getEquivalentStyles(T originalValue);
}
