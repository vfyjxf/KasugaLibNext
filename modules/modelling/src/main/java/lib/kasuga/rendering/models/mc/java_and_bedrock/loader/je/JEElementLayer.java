package lib.kasuga.rendering.models.mc.java_and_bedrock.loader.je;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lib.kasuga.rendering.models.mc.java_and_bedrock.loader.CubeVerticesMapper;
import lib.kasuga.rendering.models.mc.util.Direction;
import lib.kasuga.rendering.models.mc.util.JsonHelper;
import lib.kasuga.rendering.models.uml.loaders.structural.Context;
import lib.kasuga.rendering.models.uml.loaders.structural.Layer;
import lib.kasuga.rendering.models.uml.math.QuaternionHelper;
import lib.kasuga.rendering.models.uml.math.Transform;
import lib.kasuga.rendering.models.uml.structure.basic.Mesh;
import lib.kasuga.rendering.models.uml.structure.basic.Vertex;
import lib.kasuga.structure.Pair;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.HashMap;

public class JEElementLayer extends Layer<JsonObject> {

    private static final float RESCALE_22_5 = (float) Math.sqrt(2 - Math.sqrt(2));
    private static final float RESCALE_45 = (float) Math.sqrt(2);
    private static final float EPSILON = 0.01f;

    @Override
    public void process(JsonObject input, Context context) {
        Vector3f from = JsonHelper.jsonToV3f(input.get("from")).mul(1/16f);
        Vector3f to = JsonHelper.jsonToV3f(input.get("to")).mul(1/16f);

        JsonObject rotationObj = input.has("rotation") ? input.getAsJsonObject("rotation") : null;
        Transform rotationTransform = new Transform();
        Vector3f rescale = new Vector3f(1, 1, 1);
        Vector3f origin = new Vector3f(0, 0, 0);
        boolean hasRotation = false;
        
        if (rotationObj != null) {
            origin = JsonHelper.jsonToV3f(rotationObj.get("origin")).mul(1/16f);
            boolean doRescale = JsonHelper.jsonToBool(rotationObj, "rescale", false);

            if (rotationObj.has("angle")) {
                float angle = rotationObj.get("angle").getAsFloat();
                String axis = rotationObj.get("axis").getAsString();
                rotationTransform = createAxisRotation(origin.x(), origin.y(), origin.z(), axis, angle);
                hasRotation = true;
                
                if (doRescale) {
                    rescale = calculateAxisRescale(axis, angle);
                }
            } else if (rotationObj.has("x") || rotationObj.has("y") || rotationObj.has("z")) {
                float x = JsonHelper.jsonToFloat(rotationObj, "x", 0f);
                float y = JsonHelper.jsonToFloat(rotationObj, "y", 0f);
                float z = JsonHelper.jsonToFloat(rotationObj, "z", 0f);
                rotationTransform = createEulerRotation(origin.x(), origin.y(), origin.z(), x, y, z);
                hasRotation = true;
                
                if (doRescale) {
                    rescale = calculateEulerRescale(x, y, z);
                }
            }
        }
        
        context.setTemp("rotation_transform", rotationTransform);
        context.setTemp("rescale", rescale);
        context.setTemp("origin", origin);
        context.setTemp("has_rotation", hasRotation);

        Vector3f size = to.sub(from);
        CubeVerticesMapper mapper = new CubeVerticesMapper(from, size);

        context.setTemp("mapper", mapper);

        HashMap<String, Object> data = context.getData();
        JsonObject faceObject = input.get("faces").getAsJsonObject();
        HashMap<JsonElement, Direction> directionMap = new HashMap<>();
        data.put("mapper", mapper);
        data.put("direction", directionMap);
        for (String key : faceObject.keySet()) {
            JsonElement faceUV = faceObject.get(key);
            Direction d = Direction.fromString(key);
            directionMap.put(faceUV, d);
            addChildProcess(faceUV, "directional_layer");
        }
    }

    @Override
    public void postProcess(JsonObject input, Context context) {
        CubeVerticesMapper mapper = (CubeVerticesMapper) context.getTemp("mapper");
        Transform rotationTransform = (Transform) context.getTemp("rotation_transform");
        Vector3f rescale = (Vector3f) context.getTemp("rescale");
        Vector3f origin = (Vector3f) context.getTemp("origin");
        boolean hasRotation = (boolean) context.getTemp("has_rotation");
        
        Pair<Collection<Mesh>, Collection<Vertex>> meshesAndVertices = mapper.build(context.getLoader());
        context.getLoader().getMeshes().addAll(meshesAndVertices.getFirst());
        
        for (Vertex vertex : meshesAndVertices.getSecond()) {
            Vector3f pos = vertex.getPosition();
            
            if (hasRotation) {
                rotationTransform.apply(pos);
                pos.sub(origin);
                pos.mul(rescale);
                pos.add(origin);
                
                for (Vector3f normal : ((HashMap<Mesh, Vector3f>) vertex.getNormals()).values()) {
                    rotationTransform.normal().transform(normal);
                }
            }
        }
        
        context.getLoader().getVertices().addAll(meshesAndVertices.getSecond());
    }

    private static Transform createAxisRotation(float ox, float oy, float oz, String axisStr, float angleDegrees) {
        Transform transform = new Transform();
        transform.translate(ox, oy, oz);
        
        float ax, ay, az;
        switch (axisStr.toLowerCase()) {
            case "x" -> { ax = 1; ay = 0; az = 0; }
            case "y" -> { ax = 0; ay = 1; az = 0; }
            case "z" -> { ax = 0; ay = 0; az = 1; }
            default -> throw new IllegalArgumentException("Invalid axis: " + axisStr);
        }
        
        float angleRadians = (float) Math.toRadians(angleDegrees);
        Quaternionf rotation = new Quaternionf().rotationAxis(angleRadians, ax, ay, az);
        transform.mul(rotation);
        
        transform.translate(-ox, -oy, -oz);
        return transform;
    }

    private static Transform createEulerRotation(float ox, float oy, float oz, float xAngle, float yAngle, float zAngle) {
        Transform transform = new Transform();
        transform.translate(ox, oy, oz);
        
        Quaternionf rotation = QuaternionHelper.fromXYZDegrees(xAngle, yAngle, zAngle);
        transform.mul(rotation);
        
        transform.translate(-ox, -oy, -oz);
        return transform;
    }

    private static Vector3f calculateAxisRescale(String axisStr, float angle) {
        float scaleFactor;
        float absAngle = Math.abs(angle);
        
        if (Math.abs(absAngle - 22.5f) < EPSILON) {
            scaleFactor = RESCALE_22_5;
        } else if (Math.abs(absAngle - 45.0f) < EPSILON) {
            scaleFactor = RESCALE_45;
        } else {
            return new Vector3f(1, 1, 1);
        }

        return switch (axisStr.toLowerCase()) {
            case "x" -> new Vector3f(1, scaleFactor, scaleFactor);
            case "y" -> new Vector3f(scaleFactor, 1, scaleFactor);
            case "z" -> new Vector3f(scaleFactor, scaleFactor, 1);
            default -> new Vector3f(1, 1, 1);
        };
    }

    private static Vector3f calculateEulerRescale(float xAngle, float yAngle, float zAngle) {
        float maxAngle = Math.max(Math.abs(xAngle), Math.max(Math.abs(yAngle), Math.abs(zAngle)));
        float scaleFactor;
        
        if (Math.abs(maxAngle - 22.5f) < EPSILON) {
            scaleFactor = RESCALE_22_5;
        } else if (Math.abs(maxAngle - 45.0f) < EPSILON) {
            scaleFactor = RESCALE_45;
        } else {
            return new Vector3f(1, 1, 1);
        }

        return new Vector3f(scaleFactor, scaleFactor, scaleFactor);
    }
}
