package lib.kasuga.internal.generator.facades;

public class RegFacade {
    public static <T> T transformObject(String transformerType, T original) {
        throw new IllegalStateException("Not compiled?");
    }

    public static <T extends Object> T modifier(Class<?> target, String modifierName, Object ...arguments) {
        throw new IllegalStateException("Not compiled?");
    }

    public static Object callConfigure(Object tBlockEntityReg, String validBlocks, Object ...args) {
        throw new IllegalStateException("Not compiled?");
    }
}
