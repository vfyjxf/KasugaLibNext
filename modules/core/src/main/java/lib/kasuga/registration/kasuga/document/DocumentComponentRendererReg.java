package lib.kasuga.registration.kasuga.document;

import lib.kasuga.KasugaLib;
import lib.kasuga.content.document.DocumentComponentRenderer;
import lib.kasuga.content.document.DocumentComponentType;
import lib.kasuga.registration.Reg;
import lib.kasuga.registration.beans.rendering.RenderingRegistry;
import lib.kasuga.registration.core.RegisterContext;
import lib.kasuga.registration.stages.RegistrationStage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class DocumentComponentRendererReg<T> extends Reg<DocumentComponentRendererReg<T>, Void> implements DocumentComponentConfigurations<DocumentComponentRendererReg<T>> {

    private final Supplier<DocumentComponentRendererSupplier<T>> renderer;

    public DocumentComponentRendererReg(Supplier<DocumentComponentRendererSupplier<T>> renderer) {
        this.renderer = renderer;
    }

    @Override
    public void register(RegisterContext<?> context) {
        context.onStage(RegistrationStage.BAKING_COMPLETE, ctx -> {
            Collection<DocumentComponentType<?>> types = transform(DocumentComponentRendererModifiers.DOCUMENT_COMPONENTS, new ArrayList<>());
            for (DocumentComponentType<?> type : types) {
                //noinspection unchecked
                KasugaLib.getContext().getBean(RenderingRegistry.class).registerDocumentComponentRenderer(
                        (DocumentComponentType<T>) type, this.renderer
                );
            }
        });
    }

    @Override
    public Void getEntry() {
        return null;
    }
}
