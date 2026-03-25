package lib.kasuga.rendering.models.uml.backend;

import lib.kasuga.rendering.models.uml.bridge.Bridge;
import lombok.Getter;

import java.util.HashMap;

public abstract class Backend<T extends Bridge, R, Q> {

    @Getter
    private final HashMap<Object, R> renderingObjects;

    public Backend() {
        this.renderingObjects = new HashMap<>();
    }

    public void add(Object key, R renderable) {
        renderingObjects.put(key, renderable);
    }

    public boolean contains(Object key) {
        return renderingObjects.containsKey(key);
    }

    public boolean remove(Object key) {
        return renderingObjects.remove(key) != null;
    }

    public void renderAllObjects(Q renderContext) {
        for (R renderable : renderingObjects.values()) {
            render(renderable, renderContext);
        }
    }

    public abstract void render(R renderable, Q renderContext);
}
