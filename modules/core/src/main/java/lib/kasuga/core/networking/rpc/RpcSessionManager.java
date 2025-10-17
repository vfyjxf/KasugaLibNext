package lib.kasuga.core.networking.rpc;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

public class RpcSessionManager<T> {
    protected AtomicLong sessionIdCounter = new AtomicLong(0);

    public record Session<T>(Long id, CompletableFuture<T> future, long timeoutAt, Player accepts) implements Comparable<Session<T>> {
        @Override
        public int compareTo(@NotNull RpcSessionManager.Session<T> o) {
            return Long.compare(this.timeoutAt, o.timeoutAt);
        }

        public <S> IdentifiedRpcPacketType<S>.Packet wrap(IdentifiedRpcPacketType<S> wrapper,S value) {
            return wrapper.wrap(id, value);
        }
    }

    protected PriorityQueue<Session<T>> sessions = new PriorityQueue<>(16);
    protected HashMap<Long, Session<T>> sessionMap = new HashMap<>(16);

    protected long nextSessionId() {
        return sessionIdCounter.incrementAndGet();
    }

    public Session<T> assign(long timeout, Player accepts) {
        long id = nextSessionId();
        Session<T> session = new Session<>(id, new CompletableFuture<>(), System.currentTimeMillis() + timeout, accepts);
        sessions.add(session);
        sessionMap.put(id, session);
        return session;
    }

    public Session<T> checkTimeout() {
        long now = System.currentTimeMillis();
        Session<T> session = sessions.peek();
        if (session != null && session.timeoutAt <= now) {
            sessions.poll();
            sessionMap.remove(session.id);
            session.future.completeExceptionally(new RpcTimeoutException("RPC Session " + session.id + " timed out."));
            return session;
        }
        return null;
    }

    public void accept(Long id, T result, Player player) {
        Session<T> session = sessionMap.get(id);
        if (session != null && (session.accepts() == null || session.accepts.equals(player))) {
            sessions.remove(session);
            sessionMap.remove(id);
            session.future.complete(result);
        }
    }

    public void reject(Long id, Exception e, Player player) {
        Session<T> session = sessionMap.get(id);
        if (session != null && (session.accepts() == null || session.accepts.equals(player))) {
            sessions.remove(session);
            sessionMap.remove(id);
            session.future.completeExceptionally(e);
        }
    }
}
