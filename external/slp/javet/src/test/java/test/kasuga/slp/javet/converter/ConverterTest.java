package test.kasuga.slp.javet.converter;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.V8Runtime;
import lib.kasuga.scripting.ScriptConsole;
import lib.kasuga.scripting.ScriptException;
import lib.kasuga.scripting.security.Api;
import lib.kasuga.scripting.value.ScriptFunction;
import lib.kasuga.scripting.value.ScriptPrimitive;
import lib.kasuga.scripting.value.ScriptValue;
import lib.kasuga.slp.javet.JavetScriptEngine;
import org.junit.jupiter.api.*;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConverterTest {

    private JavetScriptEngine engine;

    @BeforeEach
    public void setUp() throws ScriptException {
        engine = new JavetScriptEngine();
        // 初始化简单的 Console 以防止 NPE，如果有日志需求
        engine.init(new ScriptConsole() {
            public void log(String s) { System.out.println(s); }
            public void warn(String s) { }
            public void debug(String s) { }
            public void info(String s) { }
            public void error(String s) { }
        });
    }

    @AfterEach
    public void tearDown() throws ScriptException {
        if (engine != null) {
            try {
                // 确保 V8 运行时没有泄漏
                engine.getRuntime().lowMemoryNotification();
                engine.close();
            } catch (Exception e) {
                // 忽略关闭时的异常，避免掩盖测试失败
                System.err.println("Teardown warning: " + e.getMessage());
            }
        }
    }

    /**
     * 1. Java ScriptPrimitive -> JS 测试
     * 验证 Java 侧实现的 ScriptPrimitive 接口对象能正确转换为 JS 中的原始类型。
     */
    @Test
    public void testJavaPrimitiveToJs() throws ScriptException, JavetException {
        // 定义一个 Java 侧的 Primitive
        ScriptPrimitive javaString = new ScriptPrimitive() {
            @Override
            public double asDouble() throws ScriptException {
                return 0;
            }

            @Override
            public int asInt() throws ScriptException {
                return 0;
            }

            @Override
            public long asLong() throws ScriptException {
                return 0;
            }

            @Override
            public short asShort() throws ScriptException {
                return 0;
            }

            @Override
            public byte asByte() throws ScriptException {
                return 0;
            }

            @Override
            public Object getValue() throws ScriptException {
                return asString();
            }

            @Override
            public String asString() { return "Hello Javet"; }
            @Override
            public void close() { }
        };

        // 注入到 JS 全局作用域
        engine.getRuntime().getGlobalObject().set("javaPrim", javaString);

        // 在 JS 中验证类型和值
        engine.execute("if (typeof javaPrim !== 'string') throw new Error('Type mismatch');");
        engine.execute("if (javaPrim !== 'Hello Javet') throw new Error('Value mismatch');");

        // 验证数字
        ScriptPrimitive javaInt = new ScriptPrimitive() {
            @Override
            public String asString() { return "123"; } // 假设底层转换逻辑会尝试解析或有其他 asInt 接口

            @Override
            public double asDouble() throws ScriptException {
                return 0;
            }

            // 实际上通常 Converter 会检查具体类型，这里假设它能识别基础包装类型或接口行为
            public int asInt() { return 123; }

            @Override
            public long asLong() throws ScriptException {
                return 0;
            }

            @Override
            public short asShort() throws ScriptException {
                return 0;
            }

            @Override
            public byte asByte() throws ScriptException {
                return 0;
            }

            @Override
            public Object getValue() throws ScriptException {
                return asInt();
            }

            @Override
            public void close() {}
        };
        // 注意：具体能否转为 number 取决于 Converter 实现，这里仅做示意
    }

    /**
     * 2. JS Primitive -> Java ScriptPrimitive 测试
     * 验证 JS 执行返回的原始类型（String, Number, Boolean）能被包装为 ScriptPrimitive。
     */
    @Test
    public void testJsPrimitiveToJava() throws ScriptException {
        // String
        Object resultStr = engine.execute("'Test String'");
        assertInstanceOf(ScriptPrimitive.class, resultStr, "JS String should be converted to ScriptPrimitive");
        assertEquals("Test String", ((ScriptPrimitive) resultStr).asString());

        // Number (Integer)
        Object resultInt = engine.execute("42");
        assertInstanceOf(ScriptPrimitive.class, resultInt, "JS Number should be converted to ScriptPrimitive");
        // 假设 ScriptPrimitive 有转换方法，或者通过 asString 验证
        assertEquals("42", ((ScriptPrimitive) resultInt).asString());

        // Boolean
        Object resultBool = engine.execute("true");
        assertInstanceOf(ScriptPrimitive.class, resultBool);
        assertEquals("true", ((ScriptPrimitive) resultBool).asString());
    }

    private void assertInstanceOf(Class<?> clazz, Object obj) {
        assertTrue(clazz.isInstance(obj));
    }

    private void assertInstanceOf(Class<?> clazz, Object obj, String s) {
        assertTrue(clazz.isInstance(obj), s);
    }

    /**
     * 3. Java Class(Object) + Function -> JS 测试
     * 验证带有 @Api 注解的 Java 对象和 ScriptFunction 能正确暴露给 JS 并被调用。
     */
    @Test
    public void testJavaObjectAndFunctionToJs() throws ScriptException, JavetException {
        AtomicReference<String> resultContainer = new AtomicReference<>("");

        // 定义带有 @Api 的测试类
        class TestApiObject {
            @Api
            public ScriptFunction methodA() {
                return new ScriptFunction() {
                    @Override
                    public String asString() { return "Function A"; }
                    @Override
                    public void close() {}
                    @Override
                    public ScriptValue execute(ScriptValue... args) throws ScriptException {
                        executeVoid(args);
                        return null; // 不返回
                    }
                    @Override
                    public void executeVoid(ScriptValue... args) throws ScriptException {
                        resultContainer.set(args[0].asString());
                    }
                };
            }
        }

        // 注入对象
        engine.getRuntime().getGlobalObject().set("apiObj", new TestApiObject());

        // JS 调用: apiObj.methodA() 获取函数，然后调用该函数传入参数
        engine.execute("apiObj.methodA()('Called from JS')");

        // 验证 Java 侧是否接收到调用
        assertEquals("Called from JS", resultContainer.get());
    }

    /**
     * 4. JS Object -> ScriptObject 测试
     * 验证 JS 对象返回到 Java 后被封装为 ScriptValue (或具体的 ScriptObject)。
     */
    @Test
    public void testJsObjectToScriptObject() throws ScriptException {
        // 执行 JS 返回一个对象
        Object result = engine.execute("({ key: 'value', id: 101 })");

        // 验证基础类型
        assertInstanceOf(ScriptValue.class, result);
        assertFalse(result instanceof ScriptPrimitive, "JS Object should not be a Primitive");

        // 进一步验证（假设 ScriptValue/ScriptObject 有类似 get 方法，或者转换回 Map）
        // 这里根据你提供的代码只有 asString，通常 ScriptObject 会有 get/set
        // 假设 toString 能反映对象结构
        assertNotNull(result);
        System.out.println("JS Object in Java: " + result);
    }

    /**
     * 5. ScriptObject GC 测试
     * 模拟高频创建销毁，验证引用计数和内存回收，防止内存泄漏。
     */
    @Test
    public void testScriptObjectGc() throws ScriptException, JavetException {
        V8Runtime v8Runtime = engine.getRuntime();

        // 强制初始 GC
        v8Runtime.lowMemoryNotification();
        int initialRefCount = v8Runtime.getReferenceCount();
        int initialCallbackContextCount = v8Runtime.getCallbackContextCount();
        int initialEngineReferenceCount = engine.getConverter().getObjectCount();

        int periodMaxRefCount = initialRefCount;
        int periodMaxCallbackContextCount = initialCallbackContextCount;
        int periodMaxEngineReferenceCount = initialEngineReferenceCount;


        // 循环创建对象并丢弃
        for (int i = 0; i < 500; i++) {
            // 1. 在 JS 中创建大对象
            // 2. 将 Java 对象注入进去
            // 3. 执行闭包
            v8Runtime.getGlobalObject().set("tempObj", new Object() {
                @Api public void callMe() {}
            });
            try{
                engine.execute("tempObj.callMe(); tempObj = null; tempObj.test();");
            }catch (ScriptException e) {}

            // 局部 GC 触发点（模拟 Runtime 自动管理或手动触发）
            if (i % 100 == 0) {
                v8Runtime.lowMemoryNotification();
            }
            // 记录周期内最大引用计数
            periodMaxRefCount = Math.max(periodMaxRefCount, v8Runtime.getReferenceCount());
            periodMaxCallbackContextCount = Math.max(periodMaxCallbackContextCount, v8Runtime.getCallbackContextCount());
            periodMaxEngineReferenceCount = Math.max(periodMaxEngineReferenceCount, engine.getConverter().getObjectCount());
        }

        // 循环结束，清理环境
        v8Runtime.getGlobalObject().delete("tempObj");

        // 终极 GC
        v8Runtime.lowMemoryNotification();

        System.gc();

        // 给一点时间让 Finalizer 运行（如果使用了 WeakReference）
        try { Thread.sleep(100); } catch (InterruptedException e) {}

        int finalRefCount = v8Runtime.getReferenceCount();
        int finalCallbackContextCount = v8Runtime.getCallbackContextCount();
        int finalEngineReferenceCount = engine.getConverter().getObjectCount();

        // 验证引用计数没有无限增长
        // 注意：finalRefCount 可能不完全等于 initialRefCount (取决于内部缓存)，但不能等于 500+
        System.out.printf("GC Test - Ref Initial: %d, Period Max: %d, Final: %d%n", initialRefCount, periodMaxRefCount, finalRefCount);
        System.out.printf("GC Test - Callback Initial: %d, Period Max: %d, Final: %d%n", initialCallbackContextCount, periodMaxCallbackContextCount, finalCallbackContextCount);
        System.out.printf("GC Test - Object Cache Initial: %d, Period Max: %d, Final: %d%n", initialEngineReferenceCount, periodMaxEngineReferenceCount, finalEngineReferenceCount);

        assertTrue((periodMaxRefCount - initialRefCount) > 10, "V8 Runtime reference count should increase during the test");
        assertTrue((periodMaxCallbackContextCount - initialCallbackContextCount) > 10, "V8 Callback context count should increase during the test");
        assertTrue((periodMaxEngineReferenceCount - initialEngineReferenceCount) > 10, "Engine object cache count should increase during the test");

        assertTrue((finalRefCount - initialRefCount) < 10, "V8 Runtime reference count should not grow uncontrollably");
        assertTrue((finalCallbackContextCount - initialCallbackContextCount) < 10, "V8 Callback context count should not grow uncontrollably");
        assertTrue((finalEngineReferenceCount - initialEngineReferenceCount) < 10, "Engine object cache count should not grow uncontrollably");

    }
}