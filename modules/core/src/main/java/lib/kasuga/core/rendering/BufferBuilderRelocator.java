package lib.kasuga.core.rendering;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class BufferBuilderRelocator {

    public static final BufferBuilderRelocator RELOCATOR = new BufferBuilderRelocator();

    private final List<BufferBuilderSupplier> suppliers;
    
    public BufferBuilderRelocator() {
        this.suppliers = new ArrayList<>();
    }
    
    public void addBufferBuilderSupplier(BufferBuilderSupplier supplier) {
        suppliers.add(supplier);
    }
    
    public @Nullable BufferBuilder getBufferBuilder(ByteBufferBuilder buffer, VertexFormat.Mode mode, VertexFormat format) {
        for (BufferBuilderSupplier supplier : suppliers) {
            BufferBuilder result = supplier.init(buffer, mode, format);
            if (result != null) return result;
        }
        return null;
    }
}
