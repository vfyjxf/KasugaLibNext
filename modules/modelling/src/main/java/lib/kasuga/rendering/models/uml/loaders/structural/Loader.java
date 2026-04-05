package lib.kasuga.rendering.models.uml.loaders.structural;

import lib.kasuga.rendering.models.uml.loaders.ModelLoader;
import lib.kasuga.rendering.models.uml.loaders.SkeletonBuilder;
import lib.kasuga.rendering.models.uml.loaders.sources.AllSources;
import lib.kasuga.rendering.models.uml.loaders.sources.SourceManager;
import lib.kasuga.rendering.models.uml.loaders.sources.SourceType;
import lib.kasuga.rendering.models.uml.structure.Model;
import lib.kasuga.rendering.models.uml.structure.basic.Mesh;
import lib.kasuga.rendering.models.uml.structure.basic.Vertex;
import lib.kasuga.rendering.models.uml.structure.basic.data.BoneBindingData;
import lib.kasuga.rendering.models.uml.structure.basic.data.mesh.MeshData;
import lib.kasuga.rendering.models.uml.structure.basic.data.vertex.VertexData;
import lib.kasuga.rendering.models.uml.structure.data.ModelData;
import lib.kasuga.rendering.models.uml.structure.material.data.TextureData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.AnchorData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.BoneData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.SkeletonData;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public abstract class Loader<
        A extends ModelData, B extends BoneData, C extends MeshData, D extends VertexData,
        E extends BoneBindingData, F extends TextureData, G extends SkeletonData, H extends AnchorData,
        I, S> implements ModelLoader<A, B, C, D, E, F, G, H, I, S> {

    private final HashMap<String, Processor<?>> processors;
    private final HashMap<SourceType, HashMap<String, SourceManager<?>>> sidedSource;

    private final List<Vertex<D, B, E>> vertices;
    private final List<Mesh<C, D, F, B, E>> meshes;
    private final SkeletonBuilder<B, H> bones;
    private final Context context;

    private final String name;

    @Nullable
    @Setter
    private A data;

    public Loader(String name) {
        this.name = name;
        this.processors = new HashMap<>();
        vertices = new ArrayList<>();
        meshes = new ArrayList<>();
        bones = new SkeletonBuilder<B, H>();
        this.context = new Context(this);
        this.data = null;
        this.sidedSource = new HashMap<>();
    }

    public <T> void registerProcessor(String name, Processor<T> processor) {
        processors.forEach((k, v) -> {
            if (v instanceof Layer<?>) {
                Layer<?> layer = (Layer<?>) v;
                layer.getNamedProcessors().put(name, processor);
            }
        });
        if (processor instanceof Layer<?> layer) {
            layer.getNamedProcessors().putAll(processors);
        }
        this.processors.put(name, processor);
    }

    public HashMap<S, Model<A, B, C, D, F, G, E, H>> load(S source, I input) {
        HashMap<S, Model<A, B, C, D, F, G, E, H>> resultMap = new HashMap<>();
        Processor<I> processor = (Processor<I>) processors.get("root");
        if (processor == null) {
            throw new IllegalStateException("No root processor registered");
        }
        processor.walk(input, context);
        build(resultMap);
        clear();
        return resultMap;
    }

    public void clear() {
        vertices.clear();
        meshes.clear();
        bones.clear();
        context.clear();
    }

    public abstract void build(HashMap<S, Model<A, B, C, D, F, G, E, H>> resultMap);

    @Override
    public HashMap<SourceType, HashMap<String, SourceManager<?>>> getSidedSources() {
        return sidedSource;
    }
}
