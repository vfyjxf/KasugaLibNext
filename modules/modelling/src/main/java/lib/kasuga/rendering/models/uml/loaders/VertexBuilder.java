package lib.kasuga.rendering.models.uml.loaders;

import lib.kasuga.rendering.models.mc.util.Direction;
import lib.kasuga.rendering.models.uml.loaders.structural.Loader;
import lib.kasuga.rendering.models.uml.structure.basic.Mesh;
import lib.kasuga.rendering.models.uml.structure.basic.Vertex;
import lib.kasuga.rendering.models.uml.structure.basic.data.BoneBindingData;
import lib.kasuga.rendering.models.uml.structure.basic.data.vertex.VertexData;
import lib.kasuga.rendering.models.uml.structure.material.Texture;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.BoneData;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Setter
@Getter
public class VertexBuilder<T extends VertexData, R extends BoneData, P extends BoneBindingData> {

    @NonNull
    private Vector3f position;

    private final HashMap<Mesh, HashMap<Texture, Vector2f>> uvs;

    private final HashMap<Mesh, Vector3f> normals;

    @Nullable
    private T data;

    private final List<String> boneNames;

    public VertexBuilder() {
        this.uvs = new HashMap<>();
        position = new Vector3f();
        this.normals = new HashMap<>();
        data = null;
        boneNames = new ArrayList<>();
    }

    public void uv(@NonNull Mesh mesh, @NonNull Texture texture, @NonNull Vector2f uv) {
        uvs.computeIfAbsent(mesh, m -> new HashMap<>()).put(texture, uv);
    }

    public void normal(@NonNull Mesh mesh, @NonNull Vector3f normal) {
        normals.put(mesh, normal);
    }

    public void normal(@NonNull Mesh mesh, Direction direction) {
        normals.put(mesh, direction.toVec3f());
    }

    public void bone(@NonNull String boneName) {
        boneNames.add(boneName);
    }

    public Vertex<T, R, P> build(Loader loader) {
        Vertex<T, R, P> vertex = new Vertex<>(position, uvs, normals, null, data);
        loader.getBones().addVertex(vertex, boneNames);
        return vertex;
    }
}
