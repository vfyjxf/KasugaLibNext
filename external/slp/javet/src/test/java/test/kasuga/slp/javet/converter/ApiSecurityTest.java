package test.kasuga.slp.javet.converter;

import com.caoccao.javet.values.reference.V8ValueObject;
import lib.kasuga.scripting.ScriptConsole;
import lib.kasuga.scripting.ScriptEngineType;
import lib.kasuga.scripting.ScriptException;
import lib.kasuga.scripting.security.*;
import lib.kasuga.scripting.value.ScriptValue;
import lib.kasuga.slp.javet.JavetScriptEngine;
import lib.kasuga.slp.javet.KasugaLibJavet;
import lib.kasuga.slp.javet.module.JsModuleResolver;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class ApiSecurityTest {

    private JavetScriptEngine engine;
    private ScriptEngineType<JavetScriptEngine> savedEngineType;
    private SecurityEngineFeature securityFeature;

    @BeforeEach
    void setUp() throws ScriptException {
        // Save and set up static ENGINE_TYPE for tests
        savedEngineType = KasugaLibJavet.ENGINE_TYPE;
        ScriptEngineType<JavetScriptEngine> engineType = ScriptEngineType.<JavetScriptEngine>builder(JavetScriptEngine::new)
            .scriptType("javascript")
            .resolver(new JsModuleResolver())
            .addFeature(SecurityEngineFeatureType.INSTANCE)
            .build();
        KasugaLibJavet.ENGINE_TYPE = engineType;

        engine = engineType.create(ScriptConsole.errorsToStderr());
        securityFeature = engine.getFeature(SecurityEngineFeatureType.INSTANCE);
        securityFeature.getConditionRegistry().register(AlwaysAllow.class, new AlwaysAllow());
    }

    @AfterEach
    void tearDown() {
        KasugaLibJavet.ENGINE_TYPE = savedEngineType;
        if (engine != null) {
            engine.close();
        }
    }

    private void setGlobal(String name, Object javaObj) throws Exception {
        V8ValueObject v8Obj = (V8ValueObject) engine.getConverter().toV8Value(engine.getRuntime(), javaObj);
        engine.getRuntime().getGlobalObject().set(name, v8Obj);
        v8Obj.close();
    }

    @Test
    public void shouldCreateValueWithAlwaysAllow() throws ScriptException {
        securityFeature.setSecurityCheckCallback((eng, params) -> true);
        ApiTestObject obj = new ApiTestObject();
        ScriptValue result = engine.createValue(obj);
        assertNotNull(result);
    }

    @Test
    public void shouldCallApiMethodWhenAllowed() throws Exception {
        securityFeature.setSecurityCheckCallback((eng, params) -> true);
        setGlobal("__obj", new ApiTestObject());
        ScriptValue result = engine.execute("__obj.greet()");
        assertNotNull(result);
        assertEquals("hello", result.asString());
    }

    @Test
    public void shouldDenyApiMethodWhenCallbackReturnsFalse() throws Exception {
        securityFeature.setSecurityCheckCallback((eng, params) -> false);
        setGlobal("__obj", new ApiTestObject());
        try {
            engine.execute("__obj.greet()");
            fail("Expected ScriptException for security denied");
        } catch (ScriptException e) {
            Throwable cause = e.getCause();
            boolean hasSecurityDenied = false;
            while (cause != null) {
                if (cause.getMessage() != null && cause.getMessage().contains("Security check denied")) {
                    hasSecurityDenied = true;
                    break;
                }
                cause = cause.getCause();
            }
            assertTrue(hasSecurityDenied, "Expected security denial but got: " + e.getMessage());
        }
    }

    @Test
    public void shouldPassParametersToSecurityCheck() throws Exception {
        Api.Parameters[][] captured = {null};
        securityFeature.setSecurityCheckCallback((eng, params) -> {
            captured[0] = params;
            return true;
        });

        setGlobal("__obj", new ApiTestObject());
        ScriptValue result = engine.execute("__obj.greet()");
        assertEquals("hello", result.asString());

        assertNotNull(captured[0], "Callback should have been called with parameters");
        assertTrue(captured[0].length > 0, "Parameters should not be empty");
        assertEquals("greeting", captured[0][0].name());
    }

    @Test
    public void shouldDenySecondMethodWhileAllowingFirst() throws Exception {
        securityFeature.setSecurityCheckCallback((eng, params) -> {
            for (Api.Parameters p : params) {
                if ("greeting".equals(p.name())) return true;
            }
            return false;
        });

        setGlobal("__obj", new ApiTestObject());
        ScriptValue result = engine.execute("__obj.greet()");
        assertEquals("hello", result.asString());

        try {
            engine.execute("__obj.secret()");
            fail("Expected ScriptException for security denied");
        } catch (ScriptException e) {
            Throwable cause = e.getCause();
            boolean hasSecurityDenied = false;
            while (cause != null) {
                if (cause.getMessage() != null && cause.getMessage().contains("Security check denied")) {
                    hasSecurityDenied = true;
                    break;
                }
                cause = cause.getCause();
            }
            assertTrue(hasSecurityDenied, "Expected security denial for secret()");
        }
    }

    @Test
    public void shouldNotExportHiddenMethod() throws Exception {
        securityFeature.setSecurityCheckCallback((eng, params) -> true);
        setGlobal("__obj", new ApiTestObject());
        ScriptValue result = engine.execute("typeof __obj.hidden");
        assertEquals("undefined", result.asString());
    }

    @Test
    public void shouldDenyWhenConditionNotRegistered() throws Exception {
        securityFeature.getConditionRegistry().clear();
        securityFeature.getConditionRegistry().register(DenyAll.class, new DenyAll());

        securityFeature.setSecurityCheckCallback((eng, params) -> true);
        setGlobal("__obj", new ApiTestObject());
        try {
            engine.execute("__obj.greet()");
            fail("Expected ScriptException");
        } catch (ScriptException e) {
            Throwable cause = e.getCause();
            boolean hasSecurityDenied = false;
            while (cause != null) {
                if (cause.getMessage() != null && cause.getMessage().contains("Security check denied")) {
                    hasSecurityDenied = true;
                    break;
                }
                cause = cause.getCause();
            }
            assertTrue(hasSecurityDenied, "Expected security denial for unregistered condition");
        }
    }

    // --- Test objects ---

    public static class ApiTestObject {

        @Api(conditions = {AlwaysAllow.class}, parameters = @Api.Parameters(name = "greeting"))
        public String greet() {
            return "hello";
        }

        @Api(conditions = {AlwaysAllow.class}, parameters = @Api.Parameters(name = "secret"))
        public String secret() {
            return "classified";
        }

        @Api(export = false)
        public String hidden() {
            return "not-exported";
        }
    }

    public static class DenyAll implements SecurityCheckCondition {
        @Override public boolean test(lib.kasuga.scripting.ScriptEngine engine, Api.Parameters[] parameters) {
            return false;
        }
    }
}
