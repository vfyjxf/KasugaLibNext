package lib.kasuga.early;

import java.util.Comparator;
import java.util.TreeSet;

public class ModLoadingManager {
    protected static TreeSet<ModLoadingProgressProvider> providers = new TreeSet<>(new Comparator<ModLoadingProgressProvider>() {
        @Override
        public int compare(ModLoadingProgressProvider o1, ModLoadingProgressProvider o2) {
            return Integer.compare(o2.priority(), o1.priority());
        }
    });

    static {
        providers.add(ModLoadingProgressProvider.FML);
        providers.add(ModLoadingProgressProvider.DEFAULT);
    }

    public static ModLoadingProgress createTask(String string, int steps) {
        for (ModLoadingProgressProvider provider : providers) {
            if(!provider.available()) {
                continue;
            }
            ModLoadingProgress progress = provider.load(string, steps);
            if(progress != null)
                return progress;
        }
        throw new IllegalStateException("WTF?");
    }
}
