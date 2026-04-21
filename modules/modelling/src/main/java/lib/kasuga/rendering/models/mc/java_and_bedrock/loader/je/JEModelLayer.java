package lib.kasuga.rendering.models.mc.java_and_bedrock.loader.je;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lib.kasuga.rendering.models.mc.util.JsonHelper;
import lib.kasuga.rendering.models.uml.loaders.structural.Context;
import lib.kasuga.rendering.models.uml.loaders.structural.Layer;

public class JEModelLayer extends Layer<JsonObject> {

    @Override
    public void process(JsonObject input, Context context) {
        context.setData("parent", JsonHelper.jsonToString(input, "parent", null));
        context.setData("ambient_occlusion", JsonHelper.jsonToBool(input, "ambientocclusion", true));
        context.setData("render_type", JsonHelper.jsonToString(input, "render_type", "solid"));
        context.setData("gui_light", JsonHelper.jsonToString(input, "gui_light", "side"));

        JsonObject textures = input.get("textures").getAsJsonObject();
        addChildProcess(textures, "texture_layer");

        JsonArray elements;
        if (!input.has("elements")) {
            throw new IllegalArgumentException("Expected 'elements' property in model JSON");
        }
        elements = input.get("elements").getAsJsonArray();
        for (JsonElement element : elements) {
            JsonObject elem = element.getAsJsonObject();
            addChildProcess(elem, "element_layer");
        }

        // TODO: handle parents
    }
}
