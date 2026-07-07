package lib.kasuga.test.scripting.security;

import lib.kasuga.scripting.ScriptConsole;
import lib.kasuga.scripting.ScriptEngine;
import lib.kasuga.scripting.ScriptEngineType;
import lib.kasuga.scripting.ScriptException;
import lib.kasuga.scripting.feature.EngineFeature;
import lib.kasuga.scripting.feature.EngineFeatureType;
import lib.kasuga.scripting.security.*;
import lib.kasuga.test.scripting.MockScriptEngine;
import net.minecraft.server.MinecraftServer;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(EphemeralTestServerProvider.class)
public class SecurityFeatureTest {

    // --- SecurityConditionRegistry ---

    @Test
    public void shouldRegisterAndResolveCondition(MinecraftServer server) {
        SecurityConditionRegistry registry = new SecurityConditionRegistry();
        AlwaysAllow alwaysAllow = new AlwaysAllow();
        registry.register(AlwaysAllow.class, alwaysAllow);

        assertSame(alwaysAllow, registry.get(AlwaysAllow.class));
        assertTrue(registry.has(AlwaysAllow.class));
    }

    @Test
    public void shouldReturnNullForUnregisteredCondition(MinecraftServer server) {
        SecurityConditionRegistry registry = new SecurityConditionRegistry();
        assertNull(registry.get(AlwaysAllow.class));
        assertFalse(registry.has(AlwaysAllow.class));
    }

    @Test
    public void shouldListRegisteredKeys(MinecraftServer server) {
        SecurityConditionRegistry registry = new SecurityConditionRegistry();
        registry.register(AlwaysAllow.class, new AlwaysAllow());

        var keys = registry.keys();
        assertEquals(1, keys.size());
        assertTrue(keys.contains(AlwaysAllow.class));
    }

    @Test
    public void shouldClearAllConditions(MinecraftServer server) {
        SecurityConditionRegistry registry = new SecurityConditionRegistry();
        registry.register(AlwaysAllow.class, new AlwaysAllow());
        registry.clear();

        assertFalse(registry.has(AlwaysAllow.class));
        assertTrue(registry.keys().isEmpty());
    }

    @Test
    public void shouldOverwritePreviousRegistration(MinecraftServer server) {
        SecurityConditionRegistry registry = new SecurityConditionRegistry();
        AlwaysAllow first = new AlwaysAllow();
        AlwaysAllow second = new AlwaysAllow();
        registry.register(AlwaysAllow.class, first);
        registry.register(AlwaysAllow.class, second);

        assertSame(second, registry.get(AlwaysAllow.class));
    }

    // --- AlwaysAllow ---

    @Test
    public void shouldAlwaysReturnTrue(MinecraftServer server) {
        AlwaysAllow allow = new AlwaysAllow();
        ScriptEngine engine = new MockScriptEngine();
        assertTrue(allow.test(engine, new Api.Parameters[0]));
        assertTrue(allow.test(engine, new Api.Parameters[]{
            new Api.Parameters() {
                @Override public Class<? extends java.lang.annotation.Annotation> annotationType() { return Api.Parameters.class; }
                @Override public String name() { return "test"; }
                @Override public String value() { return "val"; }
            }
        }));
    }

    // --- SecurityEngineFeature.check ---

    @Test
    public void shouldAllowWhenNoConditionsAndNoCallback(MinecraftServer server) {
        SecurityEngineFeature feature = new SecurityEngineFeature();
        ScriptEngine engine = new MockScriptEngine();

        Api api = createApi(new Class[0], new Api.Parameters[0]);
        assertTrue(feature.check(engine, api));
    }

    @Test
    public void shouldDenyWhenConditionNotRegistered(MinecraftServer server) {
        SecurityEngineFeature feature = new SecurityEngineFeature();
        ScriptEngine engine = new MockScriptEngine();

        Api api = createApi(new Class[]{AlwaysAllow.class}, new Api.Parameters[0]);
        assertFalse(feature.check(engine, api));
    }

    @Test
    public void shouldAllowWhenConditionPasses(MinecraftServer server) {
        SecurityEngineFeature feature = new SecurityEngineFeature();
        feature.getConditionRegistry().register(AlwaysAllow.class, new AlwaysAllow());
        ScriptEngine engine = new MockScriptEngine();

        Api api = createApi(new Class[]{AlwaysAllow.class}, new Api.Parameters[0]);
        assertTrue(feature.check(engine, api));
    }

    @Test
    public void shouldDenyWhenConditionFails(MinecraftServer server) {
        DenyAll denyAll = new DenyAll();
        SecurityEngineFeature feature = new SecurityEngineFeature();
        feature.getConditionRegistry().register(DenyAll.class, denyAll);
        ScriptEngine engine = new MockScriptEngine();

        Api api = createApi(new Class[]{DenyAll.class}, new Api.Parameters[0]);
        assertFalse(feature.check(engine, api));
    }

    @Test
    public void shouldCallCallbackAfterConditionsPass(MinecraftServer server) {
        SecurityEngineFeature feature = new SecurityEngineFeature();
        feature.getConditionRegistry().register(AlwaysAllow.class, new AlwaysAllow());

        boolean[] callbackCalled = {false};
        feature.setSecurityCheckCallback((eng, params) -> {
            callbackCalled[0] = true;
            return true;
        });

        ScriptEngine engine = new MockScriptEngine();
        Api api = createApi(new Class[]{AlwaysAllow.class}, new Api.Parameters[0]);
        assertTrue(feature.check(engine, api));
        assertTrue(callbackCalled[0]);
    }

    @Test
    public void shouldDenyWhenCallbackReturnsFalse(MinecraftServer server) {
        SecurityEngineFeature feature = new SecurityEngineFeature();
        feature.getConditionRegistry().register(AlwaysAllow.class, new AlwaysAllow());
        feature.setSecurityCheckCallback((eng, params) -> false);

        ScriptEngine engine = new MockScriptEngine();
        Api api = createApi(new Class[]{AlwaysAllow.class}, new Api.Parameters[0]);
        assertFalse(feature.check(engine, api));
    }

    @Test
    public void shouldPassParametersToCallback(MinecraftServer server) {
        SecurityEngineFeature feature = new SecurityEngineFeature();
        feature.getConditionRegistry().register(AlwaysAllow.class, new AlwaysAllow());

        Api.Parameters[][] received = {null};
        feature.setSecurityCheckCallback((eng, params) -> {
            received[0] = params;
            return true;
        });

        ScriptEngine engine = new MockScriptEngine();
        Api.Parameters[] expected = createParameters("name", "value");
        Api api = createApi(new Class[]{AlwaysAllow.class}, expected);
        feature.check(engine, api);

        assertSame(expected, received[0]);
    }

    @Test
    public void shouldPassParametersToCondition(MinecraftServer server) {
        CaptureCondition captureCondition = new CaptureCondition();

        SecurityEngineFeature feature = new SecurityEngineFeature();
        feature.getConditionRegistry().register(CaptureCondition.class, captureCondition);
        ScriptEngine engine = new MockScriptEngine();
        Api.Parameters[] expected = createParameters("key", "val");
        Api api = createApi(new Class[]{CaptureCondition.class}, expected);
        feature.check(engine, api);

        assertSame(expected, captureCondition.receivedParams);
    }

    @Test
    public void shouldSkipCallbackWhenConditionFails(MinecraftServer server) {
        DenyAll denyAll = new DenyAll();
        SecurityEngineFeature feature = new SecurityEngineFeature();
        feature.getConditionRegistry().register(DenyAll.class, denyAll);

        boolean[] callbackCalled = {false};
        feature.setSecurityCheckCallback((eng, params) -> {
            callbackCalled[0] = true;
            return true;
        });

        ScriptEngine engine = new MockScriptEngine();
        Api api = createApi(new Class[]{DenyAll.class}, new Api.Parameters[0]);
        assertFalse(feature.check(engine, api));
        assertFalse(callbackCalled[0]);
    }

    @Test
    public void shouldAllowWhenNoConditionsButCallbackReturnsTrue(MinecraftServer server) {
        SecurityEngineFeature feature = new SecurityEngineFeature();
        feature.setSecurityCheckCallback((eng, params) -> true);

        ScriptEngine engine = new MockScriptEngine();
        Api api = createApi(new Class[0], new Api.Parameters[0]);
        assertTrue(feature.check(engine, api));
    }

    @Test
    public void shouldDenyWhenNoConditionsButCallbackReturnsFalse(MinecraftServer server) {
        SecurityEngineFeature feature = new SecurityEngineFeature();
        feature.setSecurityCheckCallback((eng, params) -> false);

        ScriptEngine engine = new MockScriptEngine();
        Api api = createApi(new Class[0], new Api.Parameters[0]);
        assertFalse(feature.check(engine, api));
    }

    // --- EngineFeatureType ---

    @Test
    public void shouldBuildDefaultFeature(MinecraftServer server) {
        EngineFeatureType<SecurityEngineFeature> type = new EngineFeatureType<>(SecurityEngineFeature.Builder::new);
        SecurityEngineFeature feature = type.build();

        assertNotNull(feature);
        assertTrue(feature instanceof SecurityEngineFeature);
    }

    @Test
    public void shouldReturnBuilder(MinecraftServer server) {
        EngineFeatureType<SecurityEngineFeature> type = new EngineFeatureType<>(SecurityEngineFeature.Builder::new);
        EngineFeature.Builder<SecurityEngineFeature> builder = type.builder();

        assertNotNull(builder);
        SecurityEngineFeature feature = builder.build();
        assertNotNull(feature);
    }

    @Test
    public void shouldBuildDistinctInstances(MinecraftServer server) {
        EngineFeatureType<SecurityEngineFeature> type = new EngineFeatureType<>(SecurityEngineFeature.Builder::new);
        SecurityEngineFeature f1 = type.build();
        SecurityEngineFeature f2 = type.build();

        assertNotSame(f1, f2);
    }

    @Test
    public void shouldUseSingletonTypeKey(MinecraftServer server) {
        assertSame(SecurityEngineFeatureType.INSTANCE, SecurityEngineFeatureType.INSTANCE);
        SecurityEngineFeature feature = SecurityEngineFeatureType.INSTANCE.build();
        assertNotNull(feature);
    }

    @Test
    public void shouldAutoBuildFeatureWhenCreatingEngine(MinecraftServer server) throws ScriptException {
        ScriptEngineType<MockScriptEngine> engineType = ScriptEngineType.<MockScriptEngine>builder(() -> new MockScriptEngine())
            .scriptType("mock")
            .addFeature(SecurityEngineFeatureType.INSTANCE)
            .build();

        MockScriptEngine engine = engineType.create(ScriptConsole.noop());

        SecurityEngineFeature feature = engine.getFeature(SecurityEngineFeatureType.INSTANCE);
        assertNotNull(feature);
        assertTrue(feature instanceof SecurityEngineFeature);
    }

    @Test
    public void shouldCreateIndependentFeatureInstancesPerEngine(MinecraftServer server) throws ScriptException {
        ScriptEngineType<MockScriptEngine> engineType = ScriptEngineType.<MockScriptEngine>builder(() -> new MockScriptEngine())
            .scriptType("mock")
            .addFeature(SecurityEngineFeatureType.INSTANCE)
            .build();

        MockScriptEngine engine1 = engineType.create(ScriptConsole.noop());
        MockScriptEngine engine2 = engineType.create(ScriptConsole.noop());

        SecurityEngineFeature f1 = engine1.getFeature(SecurityEngineFeatureType.INSTANCE);
        SecurityEngineFeature f2 = engine2.getFeature(SecurityEngineFeatureType.INSTANCE);
        assertNotNull(f1);
        assertNotNull(f2);
        assertNotSame(f1, f2);
    }

    // --- helpers ---

    private static Api createApi(Class<? extends SecurityCheckCondition>[] conditions, Api.Parameters[] parameters) {
        return new Api() {
            @Override public Class<? extends java.lang.annotation.Annotation> annotationType() { return Api.class; }
            @Override public boolean export() { return true; }
            @Override public Parameters[] parameters() { return parameters; }
            @Override public Class<? extends SecurityCheckCondition>[] conditions() { return conditions; }
        };
    }

    private static Api.Parameters[] createParameters(String name, String value) {
        return new Api.Parameters[]{
            new Api.Parameters() {
                @Override public Class<? extends java.lang.annotation.Annotation> annotationType() { return Api.Parameters.class; }
                @Override public String name() { return name; }
                @Override public String value() { return value; }
            }
        };
    }

    // Dummy condition classes for registry keys
    static class DenyAll implements SecurityCheckCondition {
        @Override public boolean test(ScriptEngine engine, Api.Parameters[] parameters) { return false; }
    }

    static class CaptureCondition implements SecurityCheckCondition {
        Api.Parameters[] receivedParams;
        @Override public boolean test(ScriptEngine engine, Api.Parameters[] parameters) {
            receivedParams = parameters;
            return true;
        }
    }
}
