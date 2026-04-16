package lib.kasuga.rendering.models.uml.bridge;

import lib.kasuga.rendering.models.uml.backend.BackendContext;
import lib.kasuga.rendering.models.uml.dynamic.ModelInstance;
import lib.kasuga.rendering.models.uml.math.binding.BoneBindingFunc;
import lib.kasuga.rendering.models.uml.structure.Model;
import lib.kasuga.rendering.models.uml.structure.basic.Mesh;
import lib.kasuga.rendering.models.uml.structure.basic.Vertex;
import lib.kasuga.rendering.models.uml.structure.basic.data.BoneBindingData;
import lib.kasuga.rendering.models.uml.structure.basic.data.mesh.MeshData;
import lib.kasuga.rendering.models.uml.structure.basic.data.vertex.VertexData;
import lib.kasuga.rendering.models.uml.structure.data.ModelData;
import lib.kasuga.rendering.models.uml.structure.data.ModelInstanceData;
import lib.kasuga.rendering.models.uml.structure.material.data.TextureData;
import lib.kasuga.rendering.models.uml.structure.skeleton.SkeletonInstance;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.AnchorData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.BoneData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.SkeletonData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.SkeletonInstanceData;

import java.util.HashMap;

public interface Bridge<R> {
    HashMap<Vertex, Vertex> transformVertices(Model model,
                                              SkeletonInstance skeleton,
                                              Vertex[] vertices);

    Mesh[] transformMeshes(Model model,
                                          SkeletonInstance skeleton,
                                          Mesh[] meshes);

    R getBackendRenderable(ModelInstance modelInstance,
                           HashMap<Vertex, Vertex> vertices, Mesh[] meshes);

    BoneBindingFunc getBoneBindingFunc(Model model,
                                          SkeletonInstance skeleton,
                                          Vertex vertex);

    BackendContext<?, R, ?, ?> getBackendContext(ModelInstance modelInstance);

    default R apply(ModelInstance modelInstance) {
        Model model = modelInstance.getModel();
        SkeletonInstance skeleton = modelInstance.getSkeletonInstance();
        Vertex[] vertices = model.getVertices();
        Mesh[] meshes = model.getMeshes();
        HashMap<Vertex, Vertex> vertexMap = transformVertices(model, skeleton, vertices);
        Mesh[] transformedMeshes = transformMeshes(model, skeleton, meshes);
        return getBackendRenderable(modelInstance, vertexMap, transformedMeshes);
    }
}
