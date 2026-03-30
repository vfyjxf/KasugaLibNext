package lib.kasuga.rendering.models.uml.backend;

import lib.kasuga.rendering.models.uml.bridge.Bridge;
import lib.kasuga.rendering.models.uml.dynamic.ModelInstance;
import lombok.Getter;
import lombok.Setter;

public abstract class BackendContext<
        BridgeType extends Bridge,
        BackendRenderableType,
        ModelInstanceType extends ModelInstance,
        BackendContextType,
        BackendTransformType> {

    @Getter
    private final BridgeType bridge;

    private BackendRenderableType cache;

    @Getter
    private final ModelInstanceType modelInstance;

    @Setter
    @Getter
    private boolean render;

    public BackendContext(BridgeType bridge, ModelInstanceType modelInstance) {
        this.bridge = bridge;
        this.modelInstance = modelInstance;
        this.cache = null;
        this.render = true;
    }

    @SuppressWarnings("unchecked")
    public BackendRenderableType apply() {
        if (cache != null) return cache;
        cache = (BackendRenderableType) bridge.apply(modelInstance);
        return cache;
    }

    public abstract BackendTransformType beforeRender(BackendContextType context);
}
