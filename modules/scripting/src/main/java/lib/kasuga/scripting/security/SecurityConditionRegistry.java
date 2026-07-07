package lib.kasuga.scripting.security;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SecurityConditionRegistry {

    private final Map<Class<? extends SecurityCheckCondition>, SecurityCheckCondition> conditions = new ConcurrentHashMap<>();

    public <T extends SecurityCheckCondition> void register(Class<T> type, T instance) {
        conditions.put(type, instance);
    }

    public SecurityCheckCondition get(Class<? extends SecurityCheckCondition> type) {
        return conditions.get(type);
    }

    public boolean has(Class<? extends SecurityCheckCondition> type) {
        return conditions.containsKey(type);
    }

    public Set<Class<? extends SecurityCheckCondition>> keys() {
        return Set.copyOf(conditions.keySet());
    }

    public void clear() {
        conditions.clear();
    }
}
