package lib.kasuga.scripting.security;

import lib.kasuga.scripting.ScriptEngine;
import lib.kasuga.scripting.feature.EngineFeature;

public class SecurityEngineFeature extends EngineFeature {

    private final SecurityConditionRegistry conditionRegistry = new SecurityConditionRegistry();
    private SecurityCheckCallback callback;

    public void setSecurityCheckCallback(SecurityCheckCallback callback) {
        this.callback = callback;
    }

    public SecurityConditionRegistry getConditionRegistry() {
        return conditionRegistry;
    }

    public boolean check(ScriptEngine engine, Api api) {
        for (Class<? extends SecurityCheckCondition> condClass : api.conditions()) {
            SecurityCheckCondition condition = conditionRegistry.get(condClass);
            if (condition == null) return false;
            if (!condition.test(engine, api.parameters())) return false;
        }
        if (callback != null) {
            return callback.check(engine, api.parameters());
        }
        return true;
    }

    public static class Builder extends EngineFeature.Builder<SecurityEngineFeature> {
        @Override
        public SecurityEngineFeature build() {
            return new SecurityEngineFeature();
        }
    }
}
