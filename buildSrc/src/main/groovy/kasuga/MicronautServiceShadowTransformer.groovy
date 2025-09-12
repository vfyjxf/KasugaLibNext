package kasuga

import com.github.jengelman.gradle.plugins.shadow.relocation.RelocateClassContext
import com.github.jengelman.gradle.plugins.shadow.relocation.Relocator
import com.github.jengelman.gradle.plugins.shadow.transformers.ResourceTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.TransformerContext
import org.apache.tools.zip.ZipEntry
import org.apache.tools.zip.ZipOutputStream
import org.gradle.api.file.FileTreeElement
import org.jetbrains.annotations.NotNull

import java.nio.file.Path
import java.nio.file.Paths

class MicronautServiceShadowTransformer implements ResourceTransformer {
    private final Map<String, byte[]> resources = new LinkedHashMap<>()

    MicronautServiceShadowTransformer() {
    }

    @Override
    boolean canTransformResource(@NotNull FileTreeElement fileTreeElement) {
        return fileTreeElement.path.startsWith("META-INF/micronaut")
    }

    @Override
    void transform(@NotNull TransformerContext transformerContext) throws IOException {
        String relocatedPath  = transformerContext.path
        String transformed = relocatedPath.split("/").collect { String segment ->
            for (final def relocator in transformerContext.relocators) {
                if(relocator.canRelocateClass(segment)) {
                    return relocator.relocateClass(new RelocateClassContext(segment))
                }
            }
            return segment
        }.join("/")
        resources[transformed] = transformerContext.getInputStream().bytes
    }

    @Override
    boolean hasTransformedResource() {
        return !resources.isEmpty()
    }

    @Override
    void modifyOutputStream(@NotNull ZipOutputStream zipOutputStream, boolean b) throws IOException {
        resources.each { String originalPath, byte[] content ->
            ZipEntry entry = new ZipEntry(originalPath)
            zipOutputStream.putNextEntry(entry)
            zipOutputStream.write(content)
            zipOutputStream.closeEntry()
        }
    }
}
