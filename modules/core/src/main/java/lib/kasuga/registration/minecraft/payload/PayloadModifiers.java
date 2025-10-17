package lib.kasuga.registration.minecraft.payload;

import lib.kasuga.registration.core.Modifier;
import lib.kasuga.registration.core.ModifierType;
import net.minecraft.Util;

import java.util.concurrent.Executor;
import java.util.function.Function;

public class PayloadModifiers {
    public static final ModifierType<String> VERSION_MODIFIER = new ModifierType<>();
    public static final ModifierType<PayloadStage> STAGE_MODIFIER = new ModifierType<>();

    public static Function<String, Modifier<String>> VERSION =
            Util.memoize(SetVersionModifier::of);

    public static Function<PayloadStage, Modifier<PayloadStage>> STAGE =
            Util.memoize(SetStageModifier::of);

    private static abstract class SetVersionModifier extends Modifier<String> {
        public static SetVersionModifier of(String value) {
            return new SetVersionModifier() {
                @Override
                public String transform(String originalValue) {
                    return value;
                }

                @Override
                public ModifierType<String> getType() {
                    return VERSION_MODIFIER;
                }
            };
        }
    }

    private static abstract class SetStageModifier extends Modifier<PayloadStage> {
        public static SetStageModifier of(PayloadStage value) {
            return new SetStageModifier() {
                @Override
                public PayloadStage transform(PayloadStage originalValue) {
                    return value;
                }

                @Override
                public ModifierType<PayloadStage> getType() {
                    return STAGE_MODIFIER;
                }
            };
        }
    }
}