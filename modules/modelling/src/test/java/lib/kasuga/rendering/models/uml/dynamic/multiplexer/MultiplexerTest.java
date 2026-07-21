package lib.kasuga.rendering.models.uml.dynamic.multiplexer;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiplexerTest {

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath("kasuga_lib", path);
    }

    private static MultiplexerInput input(int power) {
        return new MultiplexerInput(Map.of(), List.of(), power, 0L, Set.of());
    }

    @Test
    void transitionsBetweenVariantsWithCrossFade() {
        Multiplexer def = Multiplexer.define(mux -> {
            MuxVariant off = mux.variant("off", v -> v.model(rl("off")));
            MuxVariant on = mux.variant("on", v -> v.model(rl("on")));
            mux.transition(off, on, t -> t.when(in -> in.redstonePower() >= 1).crossFade(0.2f));
            mux.transition(on, off, t -> t.when(in -> in.redstonePower() < 1).crossFade(0.2f));
            mux.initial(off);
        });

        MuxState state = def.newState();
        assertSame(def.variant("off"), state.current());
        assertFalse(state.inTransition());

        MultiplexerInput powered = input(15);
        def.advance(state, powered, 0.05f);   // guard holds -> start cross-fade off -> on
        assertTrue(state.inTransition());
        assertSame(def.variant("off"), state.from());
        assertSame(def.variant("on"), state.to());
        assertTrue(state.alpha() < 1f);

        for (int i = 0; i < 5; i++) {         // 0.25s elapsed > 0.2s -> commits
            def.advance(state, powered, 0.05f);
        }
        assertFalse(state.inTransition());
        assertSame(def.variant("on"), state.current());
    }

    @Test
    void instantSwitchWhenCrossFadeZero() {
        Multiplexer def = Multiplexer.define(mux -> {
            MuxVariant a = mux.variant("a", v -> v.model(rl("a")));
            MuxVariant b = mux.variant("b", v -> v.model(rl("b")));
            mux.transition(a, b, t -> t.when(in -> in.redstonePower() > 0));
            mux.initial(a);
        });

        MuxState state = def.newState();
        def.advance(state, input(5), 0.05f);
        assertSame(def.variant("b"), state.current());
        assertFalse(state.inTransition());
    }

    @Test
    void definitionIsStatelessAndShareablePerBlockType() {
        MuxRegistry registry = new MuxRegistry();
        ResourceLocation blockId = rl("fan_block");
        Multiplexer def = Multiplexer.define(mux -> {
            MuxVariant x = mux.variant("x", v -> v.model(rl("x")));
            mux.initial(x);
        });

        Multiplexer shared = registry.resolve(blockId, id -> def);
        assertSame(shared, registry.resolve(blockId, id -> def));

        // one stateless definition, independent per-instance external states
        MuxState s1 = shared.newState();
        MuxState s2 = shared.newState();
        assertNotSame(s1, s2);
        assertSame(def.variant("x"), s1.current());
        assertSame(def.variant("x"), s2.current());
    }
}
