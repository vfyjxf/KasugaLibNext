package test.kasuga.slp.javet;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.loader.JavetLibLoader;
import lib.kasuga.scripting.ScriptConsole;
import lib.kasuga.scripting.ScriptException;
import lib.kasuga.scripting.security.Api;
import lib.kasuga.scripting.value.ScriptFunction;
import lib.kasuga.scripting.value.ScriptPrimitive;
import lib.kasuga.scripting.value.ScriptValue;
import lib.kasuga.slp.javet.downloader.JavetDownloader;
import lib.kasuga.slp.javet.JavetScriptEngine;
import lib.kasuga.slp.javet.KasugaLibJavet;
import net.neoforged.fml.common.Mod;

@Mod(KasugaLibJavet.MODID)
public class TestJavetModule {
    protected JavetScriptEngine engine = new JavetScriptEngine();
    public TestJavetModule() {
        JavetLibLoader.setLibLoadingListener(new JavetDownloader());
        try{
            this.test();
        }catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }

    private void test() throws ScriptException {
        System.out.println("Test javet module!");
        TestClass t = new TestClass();
        engine.init(new ScriptConsole() {
            @Override
            public void log(String message) {
                System.out.println(message);
            }

            @Override
            public void warn(String message) {
                System.out.println(message);
            }

            @Override
            public void debug(String message) {
                System.out.println(message);
            }

            @Override
            public void info(String message) {
                System.out.println(message);
            }

            @Override
            public void error(String message) {
                System.out.println(message);
            }
        });

        try {
            engine.getRuntime().getGlobalObject().set("test", t);
            engine.execute("test.a()(42, 39, 128)");
        } catch (JavetException e) {
            throw new ScriptException(e);
        }


        engine.getRuntime().lowMemoryNotification();

        int beforeCallback = engine.getRuntime().getCallbackContextCount();
        int beforeRef = engine.getRuntime().getReferenceCount();

        try {
            for(int i=0;i<256;i++) {
                engine.getRuntime().getGlobalObject().set("test", new TestClass());
                engine.execute("test.a()(42, 39, 128)");

                engine.getRuntime().terminateExecution();

                int afterCallback = engine.getRuntime().getCallbackContextCount();
                int afterRef = engine.getRuntime().getReferenceCount();
                System.out.println("Iteration " + i + ": Callbacks after: " + afterCallback);
                System.out.println("Iteration " + i + ": References after: " + afterRef);
            }
        } catch (JavetException e) {
            throw new ScriptException(e);
        }

        engine.getRuntime().getV8Inspector().addListeners();

        engine.getRuntime().lowMemoryNotification();

        int afterCallback = engine.getRuntime().getCallbackContextCount();
        int afterRef = engine.getRuntime().getReferenceCount();

        System.out.println("Callback contexts before: " + beforeCallback + ", after: " + afterCallback);
        System.out.println("References before: " + beforeRef + ", after: " + afterRef);
    }

    protected class TestClass {
        @Api
        public ScriptFunction a() throws ScriptException {
            return new ScriptFunction(){

                @Override
                public String asString() throws ScriptException {
                    return "Fake function";
                }

                @Override
                public void close() throws ScriptException {}

                @Override
                public ScriptValue execute(ScriptValue... arguments) throws ScriptException {
                    b(arguments[0]);
                    return null;
                }

                @Override
                public void executeVoid(ScriptValue... arguments) throws ScriptException {
                    execute(arguments);
                }
            };
        }

        @Api
        public void b(ScriptValue value) throws ScriptException {
            if(value instanceof ScriptPrimitive primitive) {
                System.out.println("The answer to the Ultimate Question of Life, the Universe, and Everything is " + value.asString());
            }
        }
    }
}
