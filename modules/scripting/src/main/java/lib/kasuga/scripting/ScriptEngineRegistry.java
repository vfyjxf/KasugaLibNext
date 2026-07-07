package lib.kasuga.scripting;

import jakarta.inject.Singleton;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.function.Supplier;

@Singleton()
public class ScriptEngineRegistry {

    protected record EngineNameWithPriority(ResourceLocation name, int priority) implements Comparable<EngineNameWithPriority> {
        @Override
        public int compareTo(EngineNameWithPriority o) {
            return Integer.compare(o.priority, this.priority);
        }
    }

    public record EngineResolveResult(
            ScriptEngineType<?> engineType,
            ResourceLocation engineName
    ) { }

    protected Map<ResourceLocation, ScriptEngineType<?>> scriptEngines = new HashMap<>();

    protected Map<String, PriorityQueue<EngineNameWithPriority>> engineLanguages = new HashMap<>();

    public ScriptEngineType<?> resolve(String requiredEngine, boolean allowLoadingIssueWhenNoEnginePresent) {
        ResourceLocation location = ResourceLocation.tryParse(requiredEngine);
        if(location != null && scriptEngines.containsKey(location)) {
            if(!allowLoadingIssueWhenNoEnginePresent) {
                ScriptEngineType<?> engineType = scriptEngines.get(location);
                if(!engineType.loadingIssues.isEmpty()) {
                    return null;
                }
            }
            return scriptEngines.get(location);
        }
        PriorityQueue<EngineNameWithPriority> queue = engineLanguages.get(requiredEngine);
        ScriptEngineType<?> firstWithIssue = null;
        if(queue != null && !queue.isEmpty()) {
            for (EngineNameWithPriority engineNameWithPriority : queue) {
                ScriptEngineType<?> engineType = scriptEngines.get(engineNameWithPriority.name);
                if(firstWithIssue == null) {
                    firstWithIssue = engineType;
                }
                if(!engineType.loadingIssues.isEmpty()) {
                    continue;
                }
                return engineType;
            }
        }
        if(allowLoadingIssueWhenNoEnginePresent && firstWithIssue != null) {
            return firstWithIssue;
        }
        return null;
    }

    public void register(ResourceLocation location, List<String> types, ScriptEngineType<?> engineType, int priority) {
        scriptEngines.put(location, engineType);
        for(String type : types) {
            engineLanguages.computeIfAbsent(type, (i) -> new PriorityQueue<>()).add(new EngineNameWithPriority(
                    location,
                    priority
            ));
        }
    }
}
