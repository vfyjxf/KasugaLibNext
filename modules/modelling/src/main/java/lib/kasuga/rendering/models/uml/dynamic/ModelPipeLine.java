package lib.kasuga.rendering.models.uml.dynamic;

import lib.kasuga.rendering.models.uml.backend.Backend;
import lib.kasuga.rendering.models.uml.bridge.Bridge;
import lib.kasuga.rendering.models.uml.loaders.ModelLoader;
import lib.kasuga.rendering.models.uml.loaders.sources.Source;
import lib.kasuga.rendering.models.uml.loaders.sources.SourceManager;
import lib.kasuga.rendering.models.uml.loaders.sources.SourceType;
import lib.kasuga.rendering.models.uml.math.Transform;
import lib.kasuga.rendering.models.uml.structure.Model;
import lib.kasuga.rendering.models.uml.structure.basic.data.BoneBindingData;
import lib.kasuga.rendering.models.uml.structure.basic.data.mesh.MeshData;
import lib.kasuga.rendering.models.uml.structure.basic.data.vertex.VertexData;
import lib.kasuga.rendering.models.uml.structure.data.ModelData;
import lib.kasuga.rendering.models.uml.structure.data.ModelInstanceData;
import lib.kasuga.rendering.models.uml.structure.material.data.TextureData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.AnchorData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.BoneData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.SkeletonData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.SkeletonInstanceData;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ModelPipeLine<SourceOutputType,
        ModelDataType extends ModelData, BoneDataType extends BoneData,
        MeshDataType extends MeshData, VertexDataType extends VertexData,
        SkeletonDataType extends SkeletonData, BoneBindingDataType extends BoneBindingData,
        AnchorDataType extends AnchorData, TextureDataType extends TextureData,
        ModelInstanceDataType extends ModelInstanceData,
        SkeletonInstanceDataType extends SkeletonInstanceData,
        BackendInputType, StorageIdentifierType, InstanceIdentifierType> {

    private final SourceManager<SourceOutputType> sourceManager;

    private final Map<SourceType, HashMap<String, SourceManager<?>>> sidedSources;

    private final ModelLoader<
            ModelDataType, BoneDataType, MeshDataType, VertexDataType,
            BoneBindingDataType, TextureDataType, SkeletonDataType,
            AnchorDataType, SourceOutputType, StorageIdentifierType> loader;

    private final Map<String, Bridge<ModelDataType, BoneDataType, SkeletonDataType,
            MeshDataType, VertexDataType, TextureDataType, SkeletonInstanceDataType,
            BoneBindingDataType, AnchorDataType, ModelInstanceDataType, BackendInputType>> bridges;

    private final Map<StorageIdentifierType, Model<
            ModelDataType, BoneDataType, MeshDataType, VertexDataType,
            TextureDataType, SkeletonDataType, BoneBindingDataType, AnchorDataType>
            > models;

    private final Map<String, Backend<Bridge, ModelInstance, BackendInputType, ?, ?>> backends;

    private final Map<Model,
            HashMap<InstanceIdentifierType, ModelInstance<
                    ModelInstanceDataType, ModelDataType, BoneDataType,
                    MeshDataType, VertexDataType, SkeletonDataType,
                    SkeletonInstanceDataType, TextureDataType, AnchorDataType,
                    BoneBindingDataType>>> modelInstances;

    private ModelPipeLine(SourceManager<SourceOutputType> sourceManager,
                          ModelLoader<
                                ModelDataType, BoneDataType, MeshDataType, VertexDataType,
                                BoneBindingDataType, TextureDataType, SkeletonDataType,
                                AnchorDataType, SourceOutputType, StorageIdentifierType> loader,
                          Map<String, Bridge<ModelDataType, BoneDataType, SkeletonDataType,
                                  MeshDataType, VertexDataType, TextureDataType, SkeletonInstanceDataType,
                                  BoneBindingDataType, AnchorDataType, ModelInstanceDataType, BackendInputType>> bridges,
                          Map<String, Backend<Bridge, ModelInstance, BackendInputType, ?, ?>> backends,
                          Map<SourceType, HashMap<String, SourceManager<?>>> sidedSources
    ) {
        this.sourceManager = sourceManager;
        this.loader = loader;
        this.bridges = bridges;
        this.backends = backends;
        this.models = new HashMap<>();
        this.modelInstances = new HashMap<>();
        this.sidedSources = sidedSources;
    }

    public Map<StorageIdentifierType, Model<ModelDataType, BoneDataType, MeshDataType,
                             VertexDataType, TextureDataType, SkeletonDataType,
                             BoneBindingDataType, AnchorDataType>
            > loadModel(Object source, @Nullable String sourceLoaderName) {
        Source<?, SourceOutputType> manager = null;
        if (sourceLoaderName != null) {
            manager = sourceManager.getSource(sourceLoaderName);
        }
        if (manager == null) {
            for (Source<?, SourceOutputType> m : sourceManager.getSources().values()) {
                if (m.isValidInput(source)) {
                    manager = m;
                    break;
                }
            }
        }
        if (manager == null) {
            throw new IllegalArgumentException("No source manager found for source: " + source);
        }
        Optional<SourceOutputType> sourceOutput = ((Source<Object, SourceOutputType>) manager).getInput(source);
        if (sourceOutput.isEmpty()) {
            throw new IllegalStateException("Source manager failed to load source: " + source);
        }
        if (loader.getSidedSources().size() != sidedSources.size()) {
            loader.getSidedSources().clear();
            loader.getSidedSources().putAll(this.sidedSources);
        }
        Map<StorageIdentifierType, Model<ModelDataType, BoneDataType, MeshDataType, VertexDataType,
                TextureDataType, SkeletonDataType, BoneBindingDataType, AnchorDataType>> map =
                loader.load((StorageIdentifierType) source, sourceOutput.get());
        models.putAll(map);
        return map;
    }

    @Nullable
    public ModelInstance<ModelInstanceDataType, ModelDataType, BoneDataType,
            MeshDataType, VertexDataType, SkeletonDataType,
            SkeletonInstanceDataType, TextureDataType, AnchorDataType,
            BoneBindingDataType> createInstance(StorageIdentifierType modelName, InstanceIdentifierType instanceIdentifier,
                                                @Nullable Transform transform,
                                                @Nullable ModelInstanceDataType instanceData,
                                                @Nullable SkeletonInstanceDataType skeletonInstanceData) {
        Model<ModelDataType, BoneDataType, MeshDataType,
                VertexDataType, TextureDataType, SkeletonDataType,
                BoneBindingDataType, AnchorDataType> model = models.get(modelName);
        if (model == null) {return null;}
        ModelInstance<ModelInstanceDataType, ModelDataType, BoneDataType,
                MeshDataType, VertexDataType, SkeletonDataType,
                SkeletonInstanceDataType, TextureDataType, AnchorDataType,
                BoneBindingDataType> instance = new ModelInstance<>(model, transform, instanceData, skeletonInstanceData);
        modelInstances.computeIfAbsent(model, k -> new HashMap<>()).put(instanceIdentifier, instance);
        return instance;
    }

    @Nullable
    public ModelInstance<ModelInstanceDataType, ModelDataType, BoneDataType,
            MeshDataType, VertexDataType, SkeletonDataType,
            SkeletonInstanceDataType, TextureDataType, AnchorDataType,
            BoneBindingDataType> getInstance(StorageIdentifierType modelName, InstanceIdentifierType instanceIdentifier) {
        HashMap<InstanceIdentifierType, ModelInstance<ModelInstanceDataType, ModelDataType, BoneDataType,
                MeshDataType, VertexDataType, SkeletonDataType,
                SkeletonInstanceDataType, TextureDataType, AnchorDataType,
                BoneBindingDataType>> instances = modelInstances.get(models.get(modelName));
        if (instances == null) {return null;}
        return instances.get(instanceIdentifier);
    }

    public boolean hasModel(StorageIdentifierType modelName) {
        return models.containsKey(modelName);
    }

    public boolean hasInstance(StorageIdentifierType modelName, InstanceIdentifierType instanceIdentifier) {
        HashMap<InstanceIdentifierType, ModelInstance<ModelInstanceDataType, ModelDataType, BoneDataType,
                MeshDataType, VertexDataType, SkeletonDataType,
                SkeletonInstanceDataType, TextureDataType, AnchorDataType,
                BoneBindingDataType>> instances = modelInstances.get(models.get(modelName));
        if (instances == null) {
            return false;
        }
        return instances.containsKey(instanceIdentifier);
    }

    public void addToRenderer(
            StorageIdentifierType modelName,
            InstanceIdentifierType instanceIdentifier,
            String bridgeName,
            String backendName) {
        Model<ModelDataType, BoneDataType, MeshDataType,
                VertexDataType, TextureDataType, SkeletonDataType,
                BoneBindingDataType, AnchorDataType> model = models.get(modelName);
        if (model == null) {
            throw new IllegalArgumentException("No model found with name: " + modelName);
        }
        ModelInstance<ModelInstanceDataType, ModelDataType, BoneDataType,
                MeshDataType, VertexDataType, SkeletonDataType,
                SkeletonInstanceDataType, TextureDataType, AnchorDataType,
                BoneBindingDataType> instance = getInstance(modelName, instanceIdentifier);
        if (instance == null) {
            throw new IllegalArgumentException("No instance found with identifier: " + instanceIdentifier);
        }
        Bridge<ModelDataType, BoneDataType, SkeletonDataType,
                MeshDataType, VertexDataType, TextureDataType, SkeletonInstanceDataType,
                BoneBindingDataType, AnchorDataType, ModelInstanceDataType, BackendInputType> bridge = bridges.get(bridgeName);
        if (bridge == null) {
            throw new IllegalArgumentException("No bridge found with name: " + bridgeName);
        }
        Backend<Bridge, ModelInstance, BackendInputType, ?, ?> backend = backends.get(backendName);
        if (backend == null) {
            throw new IllegalArgumentException("No backend found with name: " + backendName);
        }
        backend.add(instance, bridge, instance);
    }

    public boolean isRendering(
            StorageIdentifierType modelName,
            InstanceIdentifierType instanceIdentifier,
            @Nullable String backendName) {
        Model<ModelDataType, BoneDataType, MeshDataType,
                VertexDataType, TextureDataType, SkeletonDataType,
                BoneBindingDataType, AnchorDataType> model = models.get(modelName);
        if (model == null) {
            return false;
        }
        ModelInstance<ModelInstanceDataType, ModelDataType, BoneDataType,
                MeshDataType, VertexDataType, SkeletonDataType,
                SkeletonInstanceDataType, TextureDataType, AnchorDataType,
                BoneBindingDataType> instance = getInstance(modelName, instanceIdentifier);
        if (instance == null) {
            return false;
        }
        if (backendName != null) {
            Backend<Bridge, ModelInstance, BackendInputType, ?, ?> backend = backends.get(backendName);
            if (backend != null) {
                return backend.contains(instance);
            }
        }
        for (Backend<Bridge, ModelInstance, BackendInputType, ?, ?> backend : backends.values()) {
            if (backend.contains(instance)) {
                return true;
            }
        }
        return false;
    }

    public boolean stopRendering(
            StorageIdentifierType modelName,
            InstanceIdentifierType instanceIdentifier,
            @Nullable String backendName) {
        Model<ModelDataType, BoneDataType, MeshDataType,
                VertexDataType, TextureDataType, SkeletonDataType,
                BoneBindingDataType, AnchorDataType> model = models.get(modelName);
        if (model == null) {
            return false;
        }
        ModelInstance<ModelInstanceDataType, ModelDataType, BoneDataType,
                MeshDataType, VertexDataType, SkeletonDataType,
                SkeletonInstanceDataType, TextureDataType, AnchorDataType,
                BoneBindingDataType> instance = getInstance(modelName, instanceIdentifier);
        if (instance == null) {
            return false;
        }
        boolean removed = false;
        if (backendName != null) {
            Backend<Bridge, ModelInstance, BackendInputType, ?, ?> backend = backends.get(backendName);
            if (backend != null) {
                return backend.remove(instance);
            }
        }
        for (Backend<Bridge, ModelInstance, BackendInputType, ?, ?> backend : backends.values()) {
            removed |= backend.remove(instance);
        }
        return removed;
    }

    public static class Builder<SourceOutputType,
        ModelDataType extends ModelData, BoneDataType extends BoneData,
        MeshDataType extends MeshData, VertexDataType extends VertexData,
        SkeletonDataType extends SkeletonData, BoneBindingDataType extends BoneBindingData,
        AnchorDataType extends AnchorData, TextureDataType extends TextureData,
        ModelInstanceDataType extends ModelInstanceData,
        SkeletonInstanceDataType extends SkeletonInstanceData,
        BackendInputType, StorageIdentifierType, InstanceIdentifierType> {

        private SourceManager<SourceOutputType> sourceManager;
        private final Map<SourceType, HashMap<String, SourceManager<?>>> sidedSources = new HashMap<>();
        private ModelLoader<
                ModelDataType, BoneDataType, MeshDataType, VertexDataType,
                BoneBindingDataType, TextureDataType, SkeletonDataType,
                AnchorDataType, SourceOutputType, StorageIdentifierType> loader;
        private final Map<String, Bridge<ModelDataType, BoneDataType, SkeletonDataType,
                MeshDataType, VertexDataType, TextureDataType, SkeletonInstanceDataType,
                BoneBindingDataType, AnchorDataType, ModelInstanceDataType, BackendInputType>> bridges = new HashMap<>();
        private final Map<String, Backend<Bridge, ModelInstance, BackendInputType, ?, ?>> backends = new HashMap<>();

        public Builder<SourceOutputType,
                ModelDataType, BoneDataType,
                MeshDataType, VertexDataType,
                SkeletonDataType, BoneBindingDataType,
                AnchorDataType, TextureDataType,
                ModelInstanceDataType,
                SkeletonInstanceDataType,
                BackendInputType, StorageIdentifierType,
                InstanceIdentifierType> withModelSource(SourceManager<SourceOutputType> modelSource) {
            this.sourceManager = modelSource;
            return this;
        }

        public Builder<SourceOutputType,
                ModelDataType, BoneDataType,
                MeshDataType, VertexDataType,
                SkeletonDataType, BoneBindingDataType,
                AnchorDataType, TextureDataType,
                ModelInstanceDataType,
                SkeletonInstanceDataType,
                BackendInputType, StorageIdentifierType,
                InstanceIdentifierType> withLoader(ModelLoader<
                        ModelDataType, BoneDataType, MeshDataType, VertexDataType,
                        BoneBindingDataType, TextureDataType, SkeletonDataType,
                        AnchorDataType, SourceOutputType, StorageIdentifierType> loader) {
            this.loader = loader;
            return this;
        }

        public Builder<SourceOutputType,
                ModelDataType, BoneDataType,
                MeshDataType, VertexDataType,
                SkeletonDataType, BoneBindingDataType,
                AnchorDataType, TextureDataType,
                ModelInstanceDataType,
                SkeletonInstanceDataType,
                BackendInputType, StorageIdentifierType,
                InstanceIdentifierType> withSidedSource(SourceType side, String name, SourceManager<?> manager) {
            this.sidedSources.computeIfAbsent(side, k -> new HashMap<>()).put(name, manager);
            return this;
        }

        public Builder<SourceOutputType,
                ModelDataType, BoneDataType,
                MeshDataType, VertexDataType,
                SkeletonDataType, BoneBindingDataType,
                AnchorDataType, TextureDataType,
                ModelInstanceDataType,
                SkeletonInstanceDataType,
                BackendInputType, StorageIdentifierType,
                InstanceIdentifierType> withBridge(String name, Bridge bridge) {
            this.bridges.put(name, bridge);
            return this;
        }

        public Builder<SourceOutputType,
                ModelDataType, BoneDataType,
                MeshDataType, VertexDataType,
                SkeletonDataType, BoneBindingDataType,
                AnchorDataType, TextureDataType,
                ModelInstanceDataType,
                SkeletonInstanceDataType,
                BackendInputType, StorageIdentifierType,
                InstanceIdentifierType> withBackend(String name, Backend backend) {
            this.backends.put(name, (Backend<Bridge, ModelInstance, BackendInputType, ?, ?>) backend);
            return this;
        }

        public ModelPipeLine<SourceOutputType,
                ModelDataType, BoneDataType,
                MeshDataType, VertexDataType,
                SkeletonDataType, BoneBindingDataType,
                AnchorDataType, TextureDataType,
                ModelInstanceDataType,
                SkeletonInstanceDataType,
                BackendInputType, StorageIdentifierType,
                InstanceIdentifierType> build() {
            Objects.requireNonNull(sourceManager);
            Objects.requireNonNull(loader);
            if (bridges.isEmpty()) {
                throw new IllegalStateException("At least one bridge must be provided");
            }
            if (backends.isEmpty()) {
                throw new IllegalStateException("At least one backend must be provided");
            }
            return new ModelPipeLine<>(sourceManager, loader, bridges, backends, sidedSources);
        }
    }
}
