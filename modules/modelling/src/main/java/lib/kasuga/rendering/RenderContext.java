package lib.kasuga.rendering;


import com.mojang.blaze3d.vertex.PoseStack;
import lib.kasuga.rendering.buffer.StencilMultiBuffer;
import lib.kasuga.structure.Pair;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;

import java.util.Stack;
import java.util.function.Consumer;

public class RenderContext {
    StencilMultiBuffer bufferSource;

    PoseStack poseStack = new PoseStack();

    public RenderContext(StencilMultiBuffer bufferSource) {
        this.bufferSource = bufferSource;
    }

    Stack<Pair<PoseStack.Pose, Consumer<RenderContext>>> stencilFunctions = new Stack<>();

    public void pushStencil(Consumer<RenderContext> stencilFunction) {
        if(!stencilFunctions.isEmpty()) {
            this.endBatch();
        }
        this.stencilFunctions.push(Pair.of(this.getPose(),stencilFunction));
    }

    public void popStencil() {
        this.endBatch();
        this.stencilFunctions.pop();
    }

    private PoseStack.Pose getPose() {
        return poseStack.last();
    }

    private void loadPose(PoseStack.Pose pose) {
        this.poseStack.last().pose().set(pose.pose());
        this.poseStack.last().normal().set(pose.normal());
    }

    public PoseStack pose() {
        return poseStack;
    }
    public void pushPose() {
        poseStack.pushPose();
        if(viewEpsilonCounter >= 0) viewEpsilonCounter++;
    }

    public void popPose() {
        poseStack.popPose();
        if(viewEpsilonCounter >= 0) viewEpsilonCounter--;
    }


    public void endBatch() {
        if(!this.stencilFunctions.isEmpty()) {
            this.applyStencil();
        }
        bufferSource.endBatch();
    }

    private void applyStencil() {
        Pair<PoseStack.Pose, Consumer<RenderContext>> pair = this.stencilFunctions.peek();
        this.pushPose();
        this.loadPose(pair.getFirst());
        pair.getSecond().accept(this);
        this.popPose();
    }

    public MultiBufferSource buffer() {
        return bufferSource;
    }

    public int light() {
        return LightTexture.FULL_BRIGHT;
    }

    public int overlay() {
        return OverlayTexture.NO_OVERLAY;
    }

    int viewEpsilonCounter = -1;

    public float viewEpsilon() {
        if(viewEpsilonCounter <= 0)
            viewEpsilonCounter = 0;
        return 0.01F;
    }

    public boolean shouldViewEpsilon() {
        return viewEpsilonCounter <= 0;
    }
}
