package test.kasuga.core.content.document;

import io.micronaut.context.annotation.Context;
import lib.kasuga.registration.kasuga.document.DocumentComponentReg;
import lib.kasuga.registration.kasuga.document.DocumentComponentRendererReg;
import lib.kasuga.registration.kasuga.document.DocumentComponentTypes;
import lib.kasuga.registration.minecraft_old.item.ItemReg;
import test.kasuga.core.CoreTestApplication;

@Context()
public class AllDocumentTestEntries {
    public static ItemReg<TestDocumentItem> testDocumentItem =
            ItemReg.of("test_document_item", TestDocumentItem::new)
                    .setParent(CoreTestApplication.registry);

    public static DocumentComponentReg<DocumentComponentTypes.DocumentString>
            testText = new DocumentComponentReg<>("test_text", DocumentComponentTypes.DocumentString::new)
            .setParent(CoreTestApplication.registry);

    public static DocumentComponentRendererReg<String>
            testTextRenderer = new DocumentComponentRendererReg<>(()-> TestDocument.CR::new)
            .withComponent(testText)
            .setParent(CoreTestApplication.registry);
}
