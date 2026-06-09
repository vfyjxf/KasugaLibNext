package lib.kasuga.rendering.models.mc.backend.vbuffer;

import com.mojang.blaze3d.vertex.VertexBuffer;
import lib.kasuga.mixins.client.AccessorVertexBuffer;
import lib.kasuga.rendering.models.mc.backend.FlatModelData;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.BitSet;

public interface IVertexBuffer extends AutoCloseable {

    VertexBuffer getVertexBuffer();

    FlatModelData getModelData();

    void uploadGpuBuffer();

    void updateGpuBuffer(@Nullable BitSet dirtyVertices, boolean forceUploadAll);

    void draw(Matrix4f modelViewMatrix, Matrix4f projectionMatrix, ShaderInstance shader);

    default int getBufferId() {
        return ((AccessorVertexBuffer) getVertexBuffer()).getVertexBufferId();
    }
}
