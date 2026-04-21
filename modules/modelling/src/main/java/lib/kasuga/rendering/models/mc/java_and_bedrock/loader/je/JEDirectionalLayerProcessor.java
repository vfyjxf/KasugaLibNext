package lib.kasuga.rendering.models.mc.java_and_bedrock.loader.je;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lib.kasuga.rendering.models.mc.java_and_bedrock.data.MCMeshData;
import lib.kasuga.rendering.models.mc.java_and_bedrock.loader.CubeVerticesMapper;
import lib.kasuga.rendering.models.mc.util.Direction;
import lib.kasuga.rendering.models.mc.util.JsonHelper;
import lib.kasuga.rendering.models.uml.loaders.structural.Context;
import lib.kasuga.rendering.models.uml.loaders.structural.Processor;
import lib.kasuga.rendering.models.uml.structure.material.Material;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.HashMap;

import static lib.kasuga.rendering.models.mc.util.Direction.DOWN;
import static lib.kasuga.rendering.models.mc.util.Direction.UP;

public class JEDirectionalLayerProcessor extends Processor<JsonObject> {
    @Override
    public void process(JsonObject input, Context context) {
        HashMap<JsonElement, Direction> directionMap = (HashMap<JsonElement, Direction>) context.getData("direction");
        Direction direction = directionMap.get(input);

        Material material = (Material) context.getData("material");
        String textureId = input.get("texture").getAsString().substring(1);

        JsonArray uvArray = input.getAsJsonArray("uv");
        Vector2f uvOrg = new Vector2f(
                uvArray.get(0).getAsFloat() / 16,
                uvArray.get(1).getAsFloat() / 16
        );
        Vector2f uvEnd = new Vector2f(
                uvArray.get(2).getAsFloat() / 16,
                uvArray.get(3).getAsFloat() / 16
        );
        Vector2f uvSize = uvEnd.sub(uvOrg);
        MCMeshData data = new MCMeshData(
                true, false, true, (boolean) context.getData("ambient_occlusion"),
                direction, new Vector4f(1f, 1f, 1f, 1f)
        );
        int rotation = JsonHelper.jsonToInt(input, "rotation", 0);
        int tint_index = JsonHelper.jsonToInt(input, "tintindex", -1);

        boolean isUpAndDown = direction == UP || direction == DOWN;
        float defaultRotation = isUpAndDown ? 270 : 90;
        CubeVerticesMapper mapper = (CubeVerticesMapper) context.getData("mapper");
        mapper.map(uvOrg, uvSize, material, direction, (rotation + defaultRotation) % 360,
                null, data, true, false);
    }
}
