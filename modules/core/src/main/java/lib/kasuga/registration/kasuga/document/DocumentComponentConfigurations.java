package lib.kasuga.registration.kasuga.document;

import lib.kasuga.registration.core.IModifierConfigure;

import java.util.List;

public interface DocumentComponentConfigurations<S extends DocumentComponentConfigurations<S>> extends IModifierConfigure<S> {
    public default S withComponent(DocumentComponentReg<?> documentComponent) {
        return configure(DocumentComponentRendererModifiers.DOCUMENT_COMPONENTS_BY_SUPPLIER.apply(()-> List.of(documentComponent.getEntry())));
    }
}
