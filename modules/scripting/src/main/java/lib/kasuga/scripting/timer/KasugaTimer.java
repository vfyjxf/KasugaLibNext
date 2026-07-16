package lib.kasuga.scripting.timer;

import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class KasugaTimer {

    public enum TimerType {
        TIMEOUT,
        INTERVAL
    }

    @FunctionalInterface
    public interface Callback {
        void tick();
    }

    public static class CallbackEntry implements Comparable<CallbackEntry> {
        final int id;
        final TimerType type;
        final Callback callback;
        long nextTicks;
        final long duration;

        CallbackEntry(int timerId, TimerType type, Callback callback, long duration, long ticks) {
            this.id = timerId;
            this.type = type;
            this.callback = callback;
            this.duration = duration;
            this.nextTicks = ticks;
        }

        @Override
        public int compareTo(CallbackEntry target) {
            return Long.compare(this.nextTicks, target.nextTicks);
        }
    }

    public final PriorityQueue<CallbackEntry> queue = new PriorityQueue<>();
    public final HashMap<Integer, CallbackEntry> queryMap = new HashMap<>();

    private final AtomicInteger counter = new AtomicInteger(1);
    private long ticksCounter = 0;

    public void onTick() {
        ticksCounter++;
        while(!queue.isEmpty() && queue.peek().nextTicks <= ticksCounter) {
            CallbackEntry entry = queue.poll();
            if(entry.type == TimerType.INTERVAL) {
                entry.nextTicks = ticksCounter + Math.max(entry.duration, 1);
                this.queue.add(entry);
            } else {
                this.queryMap.remove(entry.id);
            }
            entry.callback.tick();
        }
    }

    public int register(TimerType type, Callback callback, int ticks) {
        int id = counter.incrementAndGet();
        enqueue(new CallbackEntry(id, type, callback, ticks, ticksCounter + ticks));
        return id;
    }

    public CallbackEntry unregister(int id) {
        CallbackEntry entry = this.queryMap.remove(id);
        if(entry != null) {
            this.queue.remove(entry);
        }
        return entry;
    }

    protected void enqueue(CallbackEntry entry) {
        this.queryMap.put(entry.id, entry);
        this.queue.add(entry);
    }

    public void close() {
        this.queue.clear();
        this.queryMap.clear();
    }
}
