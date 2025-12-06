package lib.kasuga.registration.kasuga.document;

import lib.kasuga.content.document.DocumentComponentRenderer;

import java.util.function.Supplier;

public interface DocumentComponentRendererSupplier<T> {
    public DocumentComponentRenderer<T> get();
}
