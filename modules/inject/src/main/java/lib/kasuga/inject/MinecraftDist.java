package lib.kasuga.inject;

import io.micronaut.context.condition.Condition;
import io.micronaut.context.condition.ConditionContext;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;

public interface MinecraftDist {
    public static class IsClient implements Condition {
        @Override
        public boolean matches(ConditionContext context) {
            return Envs.isClient();
        }
    }

    public static class IsDedicatedServer implements Condition {
        @Override
        public boolean matches(ConditionContext context) {
            return Envs.isDedicatedServer();
        }
    }

    public static class IsDevelopment implements Condition {
        @Override
        public boolean matches(ConditionContext context) {
            return Envs.isDevEnvironment();
        }
    }

    public class Never implements Condition {
        @Override
        public boolean matches(ConditionContext context) {
            return false;
        }
    }

    public abstract class ModLoaded implements Condition {
        @Override
        public boolean matches(ConditionContext context) {
            return ModList.get().isLoaded(getModId());
        }

        public abstract String getModId();
    }
}
