package lib.kasuga.scripting;

import lib.kasuga.scripting.feature.EngineFeature;
import lib.kasuga.scripting.feature.EngineFeatureType;
import lombok.Builder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ScriptEngineType<T extends ScriptEngine> {
    public final String scriptType;
    public final Supplier<T> engineSupplier;
    public final boolean multiThreadSupporting;
    public final int priority;
    protected final Map<EngineFeatureType<?>, Function<T, ? extends EngineFeature>> features;
    public ScriptEngineType(String scriptType, Supplier<T> engineSupplier, boolean multiThreadSupporting, int priority, Map<EngineFeatureType<?>, Function<T, ? extends EngineFeature>> features) {
        this.scriptType = scriptType;
        this.engineSupplier = engineSupplier;
        this.multiThreadSupporting = multiThreadSupporting;
        this.priority = priority;
        this.features = Map.copyOf(features);
    }
}
