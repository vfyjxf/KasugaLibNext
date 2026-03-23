package lib.kasuga.rendering.models.mc.source.model;

import com.google.gson.JsonObject;
import lib.kasuga.rendering.models.uml.loaders.sources.Source;
import net.minecraft.resources.ResourceLocation;

public abstract class JsonModelSource<T> implements Source<T, JsonObject> {

    private final String name;

    public JsonModelSource(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Class<JsonObject> getOutputType() {
        return JsonObject.class;
    }
}
