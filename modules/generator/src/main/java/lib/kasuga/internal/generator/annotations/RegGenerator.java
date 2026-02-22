package lib.kasuga.internal.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

public @interface RegGenerator {

    Modifier[] modifiers() default {};

    @Target(ElementType.TYPE)
    public @interface Modifier {
        String type();
        Class<?> target();
        String extendedType() default "";
        String[] enumeration() default {};
        String prefix() default "";
    }


    @Target(ElementType.METHOD)
    public @interface ModifyFunction {
        String type();
        String[] parameterTypes() default {};
    }

    @Target(ElementType.METHOD)
    public @interface ChildrenConfiguration {
        Class<?> target() default Object.class;
    }

    @Target(ElementType.METHOD)
    public @interface ChildrenFindConfiguration {}

    @Target(ElementType.PARAMETER)
    public @interface SelfReference {}

    @Target({ElementType.TYPE})
    public @interface Type {}

    @Target(ElementType.METHOD)
    public @interface ModifierApplier {
        String type();
    }

    public @interface ModifierInstance {
        String type();
    }

    public @interface ConfigureMember {
    }
}
