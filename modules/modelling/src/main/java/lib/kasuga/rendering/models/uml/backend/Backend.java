package lib.kasuga.rendering.models.uml.backend;

import lib.kasuga.rendering.models.uml.bridge.Bridge;
import lib.kasuga.rendering.models.uml.dynamic.ModelInstance;
import lombok.Getter;

import java.util.HashMap;

public abstract class Backend<T extends Bridge, K extends ModelInstance, R, Q, E> {

    @Getter
    private final HashMap<Object, BackendContext<T, R, K, Q, E>> renderingObjects;

    public Backend() {
        this.renderingObjects = new HashMap<>();
    }

    public void add(Object key, Bridge bridge, K instance) {
        BackendContext context = bridge.getBackendContext(instance);
        context.apply();
        renderingObjects.put(key, context);
    }

    public boolean contains(Object key) {
        return renderingObjects.containsKey(key);
    }

    public boolean remove(Object key) {
        return renderingObjects.remove(key) != null;
    }

    public void renderAllObjects(Q renderContext) {
        for (BackendContext<T, R, K, Q, E> renderable : renderingObjects.values()) {
            render(renderable, renderContext);
        }
    }

    public abstract void render(BackendContext<T, R, K, Q, E> renderable, Q renderContext);
}
