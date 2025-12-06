package lib.kasuga.scripting.security;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Api {
//    boolean export() default true;
//    Class<? extends SecurityCheckCondition> security() default SecurityCheckCondition.class;
}
