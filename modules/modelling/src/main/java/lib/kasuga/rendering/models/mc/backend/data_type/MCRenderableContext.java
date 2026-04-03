package lib.kasuga.rendering.models.mc.backend.data_type;

import lib.kasuga.rendering.models.mc.backend.KsgVertexBuffer;
import lib.kasuga.rendering.models.mc.backend.MCBackend;
import lib.kasuga.rendering.models.mc.backend.MCBackendContext;
import lib.kasuga.rendering.models.mc.backend.MCBridge;
import lib.kasuga.rendering.models.uml.backend.BackendContext;
import lib.kasuga.rendering.models.uml.dynamic.ModelInstance;
import org.joml.Vector3f;

public class MCRenderableContext extends BackendContext<MCBridge, KsgVertexBuffer, ModelInstance, MCBackendContext, MCBackend.BackendTransform> {

    public MCRenderableContext(MCBridge bridge, ModelInstance modelInstance) {
        super(bridge, modelInstance);
    }

    @Override
    public MCBackend.BackendTransform beforeRender(MCBackendContext context) {
        return new MCBackend.BackendTransform(
                new Vector3f(), null, null,
                false, false, true, true,
                1f, 1f, -1, -1
        );
    }
}
