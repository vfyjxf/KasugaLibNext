package lib.kasuga.rendering.models.mc.source.model;

import com.google.gson.JsonObject;
import lib.kasuga.rendering.models.mc.source.texture.KasugaTextureManager;
import lib.kasuga.rendering.models.uml.dynamic.ModelPipeLine;
import lib.kasuga.rendering.models.uml.loaders.sources.SourceManager;
import lib.kasuga.rendering.models.uml.loaders.sources.SourceType;
import lombok.NonNull;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class KasugaModelManager extends SourceManager<JsonObject> implements PreparableReloadListener, AutoCloseable {

    private final Collection<KasugaTextureManager> textureManagers;
    private final Collection<Object> inputIdentifiers;
    private final Supplier<ModelPipeLine> pipeLineSup;

    public KasugaModelManager(SourceType type, Collection<KasugaTextureManager> textureManagers, String name, Supplier<ModelPipeLine> pipeLineSup) {
        super(type, name);
        this.textureManagers = textureManagers;
        this.inputIdentifiers = new ArrayList<>();
        this.pipeLineSup = pipeLineSup;
    }

    public void registerModel(Object inputIdentifier) {
        this.inputIdentifiers.add(inputIdentifier);
    }

    @Override
    public void close() throws Exception {}

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier barrier,
                                          @NonNull ResourceManager resourceManager,
                                          @NonNull ProfilerFiller preparationProfiler,
                                          @NonNull ProfilerFiller reloadProfiler,
                                          @NonNull Executor backgroundExecutor,
                                          @NonNull Executor gameExecutor) {
        ModelPipeLine pipe = pipeLineSup.get();
        inputIdentifiers.add(
                ResourceLocation.tryBuild(
                        "kasuga_lib",
                        "models/be/test_model_complicate.geo.json"
                )
        );
        CompletableFuture<Void> future =
                CompletableFuture.runAsync(() -> loadAllModels(pipe), backgroundExecutor);
        CompletableFuture<Void> allTexturesFuture = future.thenCompose(ignored -> {
            List<CompletableFuture<Void>> textureFutures = textureManagers.stream()
                    .map(manager -> manager.reload(barrier, resourceManager, preparationProfiler, reloadProfiler, backgroundExecutor, gameExecutor))
                    .toList();
            return CompletableFuture.allOf(textureFutures.toArray(new CompletableFuture[0]));
        });
        return allTexturesFuture;
    }

    private void loadAllModels(ModelPipeLine pipe) {
        for (Object inputIdentifier : this.inputIdentifiers) {
            pipe.loadModel(inputIdentifier, null);
        }
    }
}
