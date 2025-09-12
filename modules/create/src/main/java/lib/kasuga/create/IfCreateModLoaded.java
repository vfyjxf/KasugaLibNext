package lib.kasuga.create;

import io.micronaut.context.condition.Condition;
import io.micronaut.context.condition.ConditionContext;
import net.neoforged.fml.ModList;

public class IfCreateModLoaded implements Condition {
    @Override
    public boolean matches(ConditionContext context) {
        context.fail("Create mod was not loaded");
        return ModList.get().isLoaded("create");
    }
}
