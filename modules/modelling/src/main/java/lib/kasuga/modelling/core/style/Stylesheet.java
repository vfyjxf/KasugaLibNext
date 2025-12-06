package lib.kasuga.modelling.core.style;

import lib.kasuga.modelling.core.element.Element;

public interface Stylesheet {
    public void apply(Element element, StyleMap.Writable target);

    public void applyImportant(Element element, StyleMap.Writable target);

    public interface Subscribable extends Stylesheet {
        void removeAssociation(StyleStore styleStore);

        void addAssociation(StyleStore styleStore);
    }
}
