package lib.kasuga.rendering.models.uml.backend;

import lib.kasuga.rendering.models.uml.bridge.Bridge;
import lib.kasuga.rendering.models.uml.dynamic.ModelInstance;
import lombok.Getter;
import lombok.Setter;

public abstract class BackendContext<
        BridgeType extends Bridge,
        BackendRenderableType,
        BackendContextType,
        BackendTransformType> implements AutoCloseable {

    @Getter
    private final BridgeType bridge;

    private BackendRenderableType cache;
    private long skeletonVersion;

    @Getter
    private final ModelInstance modelInstance;

    @Setter
    @Getter
    private boolean render;

    public BackendContext(BridgeType bridge, ModelInstance modelInstance) {
        this.bridge = bridge;
        this.modelInstance = modelInstance;
        this.cache = null;
        this.skeletonVersion = Long.MIN_VALUE;
        this.render = true;
    }

    @SuppressWarnings("unchecked")
    public BackendRenderableType apply() {
        modelInstance.getSkeletonInstance().tick();
        long currentVersion = modelInstance.getSkeletonInstance().getVersion();
        if (cache != null && skeletonVersion == currentVersion) return cache;
        if (cache instanceof VersionedBackendRenderable versioned) {
            versioned.updateForVersion(modelInstance, bridge);
            skeletonVersion = currentVersion;
            return cache;
        }
        closeCache();
        cache = (BackendRenderableType) bridge.apply(modelInstance);
        skeletonVersion = currentVersion;
        return cache;
    }

    private void closeCache() {
        if (cache instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (Exception ignored) {}
        }
        cache = null;
    }

    public abstract BackendTransformType beforeRender(BackendContextType context);

    @Override
    public void close() throws Exception {
        closeCache();
    }
}
