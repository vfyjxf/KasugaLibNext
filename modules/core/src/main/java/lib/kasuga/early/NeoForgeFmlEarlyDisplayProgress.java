package lib.kasuga.early;

import net.neoforged.fml.loading.progress.ProgressMeter;

public class NeoForgeFmlEarlyDisplayProgress implements ModLoadingProgress {
    ProgressMeter meter;
    public NeoForgeFmlEarlyDisplayProgress(ProgressMeter meter) {
        this.meter = meter;
    }
    @Override
    public void increment() {
        meter.increment();
    }

    @Override
    public void complete() {
        meter.complete();
    }
    @Override
    public void set(int i) {
        meter.setAbsolute(i);
    }

    @Override
    public int steps() {
        return meter.steps();
    }

    @Override
    public void label(String title) {
        meter.label(title);
    }
}
