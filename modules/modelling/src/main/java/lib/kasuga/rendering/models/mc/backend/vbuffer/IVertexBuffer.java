package lib.kasuga.rendering.models.mc.backend.vbuffer;

import com.mojang.blaze3d.vertex.VertexBuffer;
import lib.kasuga.rendering.models.mc.backend.FlatModelData;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;

public interface IVertexBuffer extends AutoCloseable {

    VertexBuffer getVertexBuffer();

    FlatModelData getModelData();

    void uploadGpuBuffer();

    void updateGpuBuffer(@Nullable BitSet dirtyVertices, boolean forceUploadAll);
}
