package lib.kasuga.early;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;

public class DefaultModLoadingProgress implements ModLoadingProgress {
    protected static final Logger LOGGER = LogUtils.getLogger();
    private String label;
    private final AtomicInteger current = new AtomicInteger();
    private final int totalSteps;

    public DefaultModLoadingProgress(String text, int totalSteps) {
        this.label = text;
        this.totalSteps = totalSteps;
    }

    @Override
    public void increment() {
        current.incrementAndGet();
        printTask();
    }

    @Override
    public void complete() {}

    @Override
    public void set(int i) {
        this.current.set(i);
        printTask();
    }

    private void printTask() {
        int n = this.current.get();
        LOGGER.info(this.label + " [" + n + "/" + totalSteps + "]");
    }

    @Override
    public int steps() {
        return this.totalSteps;
    }

    @Override
    public void label(String title) {
        this.label = title;
        printTask();
    }
}
