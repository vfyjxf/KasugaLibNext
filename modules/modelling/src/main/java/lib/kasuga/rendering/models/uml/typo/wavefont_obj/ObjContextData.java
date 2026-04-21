package lib.kasuga.rendering.models.uml.typo.wavefont_obj;

import lib.kasuga.rendering.models.uml.loaders.serial.ContextData;
import lib.kasuga.rendering.models.uml.loaders.serial.SerialContext;
import lib.kasuga.rendering.models.uml.math.Transform;
import lib.kasuga.rendering.models.uml.math.binding.BoneBindingFunc;
import lib.kasuga.rendering.models.uml.structure.basic.BoneBinding;
import lib.kasuga.rendering.models.uml.structure.basic.Mesh;
import lib.kasuga.rendering.models.uml.structure.basic.Vertex;
import lib.kasuga.rendering.models.uml.structure.material.Material;
import lib.kasuga.rendering.models.uml.structure.skeleton.Bone;
import lib.kasuga.structure.Pair;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.*;

public class ObjContextData implements ContextData<ObjContextData> {

    @Getter
    private final String name;

    @Getter
    private final ArrayList<Vector3f> vertexPositions, vertexNormals;

    @Getter
    private final ArrayList<Vector2f> vertexUvs;

    @Getter
    private final ArrayList<ArrayList<ObjVertexBinding>> bindings;

    @Getter
    private String mtlName;

    @Getter
    @Setter
    private boolean smoothShading;

    @Getter
    private final boolean isGroup;

    public ObjContextData(String name, boolean isGroup) {
        this.name = name;
        vertexPositions = new ArrayList<>();
        vertexNormals = new ArrayList<>();
        vertexUvs = new ArrayList<>();
        this.bindings = new ArrayList<>();
        mtlName = null;
        smoothShading = false;
        this.isGroup = isGroup;
    }

    public void v(Vector3f vertex) {
        vertexPositions.add(vertex);
    }

    public void vn(Vector3f normal) {
        vertexNormals.add(normal);
    }

    public void useMtl(String mtlName) {
        this.mtlName = mtlName;
    }

    public void vt(Vector2f uv) {
        vertexUvs.add(uv);
    }

    public void s(boolean smooth) {
        this.smoothShading = smooth;
    }

    public void f(ArrayList<ObjVertexBinding> vertexBindings) {
        if (vertexBindings.size() < 2) {
            throw new IllegalArgumentException("Face must have at least 2 vertices");
        }
        bindings.add(vertexBindings);
    }

    @Override
    public void build(SerialContext<ObjContextData> context) {
    }

    public void buildVertexAndMesh(ObjModelLoader loader, Bone bone) {
        String boneName = bone.getName();
        ArrayList<Pair<ObjVertexBinding, Pair<Integer, Integer>>>[] verticesAndMeshes = new ArrayList[vertexPositions.size()];
        Mesh[] meshes = new Mesh[bindings.size()];
        for (int i = 0; i < bindings.size(); i++) {
            ArrayList<ObjVertexBinding> faces = bindings.get(i);
            meshes[i] = new Mesh(new Vertex[faces.size()], new Vector3f(), new Transform(),
                    new Material[]{loader.getMaterial(this, loader, mtlName)},
                    loader.getMeshData(this, loader));
            int j = 0;
            for (ObjVertexBinding binding : faces) {
                int vIndex = binding.vertexIndex();
                if (verticesAndMeshes[vIndex] == null) {
                    verticesAndMeshes[vIndex] = new ArrayList<>();
                }
                ArrayList<Pair<ObjVertexBinding, Pair<Integer, Integer>>> m = verticesAndMeshes[vIndex];
                m.add(Pair.of(binding, Pair.of(i, j)));
                j++;
            }
        }
        List<Vertex> vertices = new ArrayList<>();
        for (ArrayList<Pair<ObjVertexBinding, Pair<Integer, Integer>>> b : verticesAndMeshes) {
            if (b == null || b.isEmpty()) continue;
            Vector3f position = null;
            int i = 0;
            Vertex vertex = null;
            for (Pair<ObjVertexBinding, Pair<Integer, Integer>> pair : b) {
                ObjVertexBinding binding = pair.getFirst();
                Pair<Integer, Integer> p = pair.getSecond();
                Mesh mesh = meshes[p.getFirst()];
                if (position == null) {
                    position = vertexPositions.get(binding.vertexIndex());
                    vertex = new Vertex(
                            position, loader.getVertexData(this, loader, boneName)
                    );
                }
                mesh.getVertices()[p.getSecond()] = vertex;
                vertex.addUV(mesh, mesh.getMaterials()[0], vertexUvs.get(binding.textureIndex()));
                vertex.getNormals().put(mesh, vertexNormals.get(binding.normalIndex()));
            }
            vertex.setBinding(new BoneBinding(new Pair[]{Pair.of(bone, 1.0f)}, BoneBindingFunc.BDEF, null));

            vertices.add(vertex);
        }
        loader.getVertices().addAll(vertices);
        loader.getMeshes().addAll(Arrays.asList(meshes));
    }
}
