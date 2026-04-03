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

public interface Bridge<
        A extends ModelData, B extends BoneData, C extends SkeletonData,
        D extends MeshData, E extends VertexData, F extends TextureData,
        G extends SkeletonInstanceData, H extends BoneBindingData, I extends AnchorData,
        J extends ModelInstanceData,
        R> {
    HashMap<Vertex, Vertex> transformVertices(Model<A, B, D, E, F, C, H, I> model,
                                              SkeletonInstance<G, C, B, H, I> skeleton,
                                              Vertex<E, B, H>[] vertices);

    Mesh<D, E, F, B, H>[] transformMeshes(Model<A, B, D, E, F, C, H, I> model,
                                          SkeletonInstance<G, C, B, H, I> skeleton,
                                          Mesh<D, E, F, B, H>[] meshes);

    R getBackendRenderable(ModelInstance<J, A, B, D, E, C, G, F, I, H> modelInstance,
                           HashMap<Vertex, Vertex> vertices, Mesh<D, E, F, B, H>[] meshes);

    BoneBindingFunc<B> getBoneBindingFunc(Model<A, B, D, E, F, C, H, I> model,
                                          SkeletonInstance<G, C, B, H, I> skeleton,
                                          Vertex<?, B, ?> vertex);

    BackendContext<?, R, ModelInstance, ?, ?> getBackendContext(ModelInstance<J, A, B, D, E, C, G, F, I, H> modelInstance);

    default R apply(ModelInstance<J, A, B, D, E, C, G, F, I, H> modelInstance) {
        Model<A, B, D, E, F, C, H, I> model = modelInstance.getModel();
        SkeletonInstance<G, C, B, H, I> skeleton = modelInstance.getSkeletonInstance();
        Vertex<E, B, H>[] vertices = model.getVertices();
        Mesh<D, E, F, B, H>[] meshes = model.getMeshes();
        HashMap<Vertex, Vertex> vertexMap = transformVertices(model, skeleton, vertices);
        Mesh<D, E, F, B, H>[] transformedMeshes = transformMeshes(model, skeleton, meshes);
        return getBackendRenderable(modelInstance, vertexMap, transformedMeshes);
    }
}
