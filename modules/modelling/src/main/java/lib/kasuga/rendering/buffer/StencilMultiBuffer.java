package lib.kasuga.rendering.buffer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import lib.kasuga.rendering.StencilUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StencilMultiBuffer implements MultiBufferSource {
    public static final RenderType STENCIL = RenderType.create(
            "stencil_mask",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            1536,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .createCompositeState(false)
    );

    protected static record Batch(
        Map<RenderType, MeshData> buffers
    ) implements AutoCloseable {
        public void draw() {
            Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
            if(buffers.containsKey(STENCIL)) {
                StencilUtils.prepareStencil();

                STENCIL.draw(buffers.get(STENCIL));
                StencilUtils.renderStencil();

                Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
            }

            for (Map.Entry<RenderType, MeshData> renderTypeMeshDataEntry : buffers.entrySet()) {
                if(renderTypeMeshDataEntry.getKey() == STENCIL) {
                    continue;
                }

                renderTypeMeshDataEntry.getKey().draw(renderTypeMeshDataEntry.getValue());
            }

            if(buffers.containsKey(STENCIL)) {
                StencilUtils.resetStencil();
            }
        }

        @Override
        public void close() {
            for (MeshData value : buffers.values()) {
                value.close();
            }
        }
    }

    List<Batch> batches = new ArrayList<>();

    Map<RenderType, ByteBufferBuilder> byteBuffers = new HashMap<>();
    Map<RenderType, BufferBuilder> buffers = new HashMap<>();

    @Override
    public @NotNull VertexConsumer getBuffer(@NotNull RenderType renderType) {
        return buffers.computeIfAbsent(renderType, (r) -> {
            ByteBufferBuilder byteBufferBuilder = byteBuffers.computeIfAbsent(r, (b)->new ByteBufferBuilder(renderType.bufferSize()));
            return new BufferBuilder(byteBufferBuilder, r.mode(), r.format());
        });
    }

    public void endBatch() {
        Map<RenderType, MeshData> meshDataMap = new HashMap<>();
        for (Map.Entry<RenderType, BufferBuilder> entry : buffers.entrySet()) {
            RenderType renderType = entry.getKey();
            BufferBuilder bufferBuilder = entry.getValue();
            MeshData meshData = bufferBuilder.build();
            meshDataMap.put(renderType, meshData);
        }
//        Batch batch = new Batch(meshDataMap);
//        batch.draw();
//        batch.close();
        this.buffers.clear();
        batches.add(new Batch(meshDataMap));
    }

    public void render() {
        for (Batch batch : this.batches) {
            batch.draw();
        }
    }

    public void discard() {
        for (Batch batch : this.batches) {
            batch.close();
        }
        batches.clear();
    }
}
