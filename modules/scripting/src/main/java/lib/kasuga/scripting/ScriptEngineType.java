package lib.kasuga.scripting;

import lib.kasuga.scripting.feature.EngineFeature;
import lib.kasuga.scripting.feature.EngineFeatureType;
import lombok.Builder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ScriptEngineType<T extends ScriptEngine> {
    public final String scriptType;
    public final Supplier<T> engineSupplier;
    public final boolean multiThreadSupporting;
    public final int priority;
    public final List<Throwable> loadingIssues;
    protected final Map<EngineFeatureType<?>, Function<T, ? extends EngineFeature>> features;
    // public final EngineModuleResolver resolver;

    public ScriptEngineType(String scriptType, Supplier<T> engineSupplier, /*EngineModuleResolver resolver, */boolean multiThreadSupporting, int priority, Map<EngineFeatureType<?>, Function<T, ? extends EngineFeature>> features) {
        this.scriptType = scriptType;
        this.engineSupplier = engineSupplier;
        this.multiThreadSupporting = multiThreadSupporting;
        this.priority = priority;
        this.features = Map.copyOf(features);
        this.loadingIssues = new ArrayList<>();
    }

    public void addLoadingIssue(Throwable issue) {
        this.loadingIssues.add(issue);
    }

    public static <T extends ScriptEngine> Builder<T> builder(Supplier<T> engineSupplier) {
        return new Builder<T>().engineSupplier(engineSupplier);
    }

    public boolean isAvailable() {
        return loadingIssues.isEmpty();
    }

    public static class Builder<T extends ScriptEngine> {

        protected String scriptType;
        protected Supplier<T> engineSupplier;
//        protected EngineModuleResolver resolver;
        protected boolean multiThreadSupporting = false;
        protected int priority = 0;
        protected final Map<EngineFeatureType<?>, Function<T, ? extends EngineFeature>> features = new HashMap<>();


        public Builder<T> scriptType(String scriptType) {
            this.scriptType = scriptType;
            return this;
        }

//        public Builder<T> resolver(EngineModuleResolver resolver) {
//            this.resolver = resolver;
//            return this;
//        }

        public Builder<T> engineSupplier(Supplier<T> engineSupplier) {
            this.engineSupplier = engineSupplier;
            return this;
        }

        public Builder<T> multiThreadSupporting(boolean multiThreadSupporting) {
            this.multiThreadSupporting = multiThreadSupporting;
            return this;
        }

        public Builder<T> priority(int priority) {
            this.priority = priority;
            return this;
        }

        public <F extends EngineFeature> Builder<T> addFeature(EngineFeatureType<F> featureType, Function<T, F> featureFunction) {
            this.features.put(featureType, featureFunction);
            return this;
        }

        public ScriptEngineType<T> build() {
            return new ScriptEngineType<>(scriptType, engineSupplier, /*resolver,*/ multiThreadSupporting, priority, features);
        }
    }
}
