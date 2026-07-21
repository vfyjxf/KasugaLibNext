package lib.kasuga.rendering.models.uml.dynamic.fsm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Verifies {@code ctx.lockLayer(id, ticks)} freezes another layer for N ticks (it still emits its pose). */
class LockLayerTest {

    static final class Actor {}

    @Test
    void lockFreezesTargetLayerForTwoTicks() {
        Actor owner = new Actor();
        StateMachine<Actor> m = StateMachine.builder(owner)
                .layer("control", layer -> {
                    State<Actor> idle = layer.state("idle");
                    State<Actor> locking = layer.state("locking").onEnter(ctx -> ctx.lockLayer("locomotion", 2));
                    layer.initial(idle);
                    layer.transition("start", idle, locking).on("go");
                })
                .layer("locomotion", layer -> {
                    State<Actor> stand = layer.state("stand");
                    State<Actor> moved = layer.state("moved");
                    layer.initial(stand);
                    layer.transition("s2m", stand, moved).when(ctx -> true);
                })
                .build();

        assertEquals("stand", m.layer("locomotion").active().id());

        m.trigger("go");
        m.tick(); // control → locking (locks locomotion); locomotion locked this tick
        assertEquals("stand", m.layer("locomotion").active().id());

        m.tick(); // still locked
        assertEquals("stand", m.layer("locomotion").active().id());

        m.tick(); // lock expired → s2m fires
        assertEquals("moved", m.layer("locomotion").active().id());
    }
}
