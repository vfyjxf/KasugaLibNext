package lib.kasuga.annotations;

import java.lang.annotation.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ConditionalMixin {
    Class<? extends MixinCondition> condition();
}
