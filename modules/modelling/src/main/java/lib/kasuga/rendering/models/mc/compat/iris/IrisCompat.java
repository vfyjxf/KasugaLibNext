package lib.kasuga.rendering.models.mc.compat.iris;

import lombok.Getter;
import net.irisshaders.iris.api.v0.IrisApi;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;

import java.util.Optional;
import java.util.function.Consumer;

public class IrisCompat {

    public static final String IRIS_MOD_ID = "iris";

    @Getter
    private static Optional<? extends ModContainer> irisModContainer;

    public static void onStart() {
        irisModContainer = ModList.get().getModContainerById(IRIS_MOD_ID);
    }

    public static boolean isIrisPresent() {
        return irisModContainer.isPresent();
    }

    public static void onIrisPresentOrElse(Consumer<ModContainer> onPresent, Runnable onAbsent) {
        irisModContainer.ifPresentOrElse(onPresent, onAbsent);
    }

    public static void onIrisPresent(Consumer<ModContainer> onPresent) {
        irisModContainer.ifPresent(onPresent);
    }

    public static void onIrisAbsent(Runnable onAbsent) {
        if (irisModContainer.isEmpty()) {
            onAbsent.run();
        }
    }

    public static boolean isUsingShaderPack() {
        return isIrisPresent() && IrisApi.getInstance().isShaderPackInUse();
    }
}
