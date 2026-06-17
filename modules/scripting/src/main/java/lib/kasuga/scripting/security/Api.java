package lib.kasuga.scripting.security;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Api {
    boolean export() default true;
    public @interface Parameters {
        String name();
        String value() default "";
    }
    Parameters[] parameters() default {};
    Class<? extends SecurityCheckCondition>[] conditions() default {};
}
