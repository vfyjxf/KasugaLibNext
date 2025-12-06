package lib.kasuga.early;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiFunction;

public interface ModLoadingProgressProvider {
    public ModLoadingProgress load(String text, int totalSteps);
    public boolean available();
    public int priority();

    public static class Default implements ModLoadingProgressProvider {
        @Override
        public ModLoadingProgress load(String text, int totalSteps) {
            return new DefaultModLoadingProgress(text, totalSteps);
        }

        @Override
        public boolean available() {
            return true;
        }

        @Override
        public int priority() {
            return -1000;
        }

        @Override
        public String toString() {
            return "Default Mod Loading Context Provider";
        }
    }

    public static class FML implements ModLoadingProgressProvider {

        protected static final BiFunction<String, Integer, ModLoadingProgress> callMethod = getInvoker();

        protected static BiFunction<String, Integer, ModLoadingProgress> getInvoker() {
            Logger logger = LogUtils.getLogger();
            try{
                ClassLoader cl = ModLoadingProgressProvider.class.getClassLoader();

                try{
                    cl.loadClass("net.neoforged.neoforge.common.NeoForge");
                }catch (ClassNotFoundException e) {
                    logger.info("NeoForge Test Failed.");
                    return null;
                }

                Class<?> managerClass = cl.loadClass("net.neoforged.fml.loading.progress.StartupNotificationManager");
                Method originalMethod = managerClass.getMethod("prependProgressBar", String.class, int.class);

                if(originalMethod.canAccess(null))
                    originalMethod.trySetAccessible();

                Class<?> progressMeterClass = ModLoadingProgressProvider.class.getClassLoader().loadClass("net.neoforged.fml.loading.progress.ProgressMeter");

                if(originalMethod.getReturnType() != progressMeterClass){
                    logger.warn("The early display's prependProgressBar mismatch the ProgressMeter class, expected" + progressMeterClass.getName() + ", got" + originalMethod.getReturnType().getName());
                    return null;
                }

                progressMeterClass.getMethod("increment");
                progressMeterClass.getMethod("complete");
                progressMeterClass.getMethod("label", String.class);
                progressMeterClass.getMethod("setAbsolute", int.class);
                Method stepsMethod = progressMeterClass.getMethod("steps");

                if(stepsMethod.getReturnType() != int.class){
                    logger.warn("The return type of early display ProgressMeter's steps function mismatch, expected return type" + int.class.getName() + ", got" + stepsMethod.getName());
                    return null;
                }

                Class<?> taskClass = cl.loadClass("lib.kasuga.early.NeoForgeFmlEarlyDisplayProgress");

                Constructor<?> constructor = taskClass.getConstructor(progressMeterClass);

                return ((i, v)->{
                    try{
                        return (ModLoadingProgress) constructor.newInstance(originalMethod.invoke(null, i, v));
                    }catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                        logger.error("Failed to construct the task class.", e);
                        return null;
                    }
                });
            } catch (ClassNotFoundException | NullPointerException | NoSuchMethodException e) {
                logger.error("Failed to loading the FML Early Display Task.", e);
            }
            return null;
        }


        @Override
        public ModLoadingProgress load(String text, int totalSteps) {
            return callMethod == null ? null : callMethod.apply(text, totalSteps);
        }

        @Override
        public boolean available() {
            return callMethod != null;
        }

        @Override
        public int priority() {
            return 255;
        }

        @Override
        public String toString() {
            return "NeoForge ModLoader EarlyDisplay Task";
        }
    }

    public static ModLoadingProgressProvider DEFAULT = new Default();
    public static ModLoadingProgressProvider FML = new FML();
}
