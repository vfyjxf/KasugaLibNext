package lib.kasuga.create;

import io.micronaut.context.annotation.Requires;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Requires(condition = IfCreateModLoaded.class)
public @interface CreateModBeans {
}
