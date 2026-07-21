package lib.kasuga.rendering.models.uml.dynamic.multiplexer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Stateless variant-graph <b>definition</b>: {@link MuxVariant} nodes + guarded transitions (with
 * optional cross-fade). Holds NO runtime state — one instance can be shared per block type. Per-instance
 * runtime state lives in a {@link MuxState} the owner holds; {@link #advance} walks the transitions and
 * updates that external state. Variants are typed handles (transitions reference handles, not string ids).
 *
 * <pre>{@code
 * Multiplexer def = Multiplexer.define(mux -> {
 *     MuxVariant off = mux.variant("off", v -> v.model(rl("off_model")));
 *     MuxVariant on  = mux.variant("on",  v -> v.model(rl("on_model")));
 *     mux.transition(off, on, t -> t.when(powerAtLeast(1)).crossFade(0.2f));
 *     mux.transition(on, off, t -> t.when(in -> in.redstonePower() < 1).crossFade(0.2f));
 *     mux.initial(off);
 * });
 * MuxState state = def.newState();          // owner holds this
 * def.advance(state, input, dt);            // owner ticks: walks transitions, advances cross-fade
 * }</pre>
 */
public final class Multiplexer {

    private final List<MuxVariant> variants;
    private final Map<String, MuxVariant> variantsById;
    private final List<Transition> transitions;
    private final MuxVariant initial;

    public Multiplexer(List<MuxVariant> variants, List<Transition> transitions, MuxVariant initial) {
        this.variants = List.copyOf(variants);
        Map<String, MuxVariant> byId = new LinkedHashMap<>();
        for (MuxVariant variant : this.variants) {
            byId.put(variant.id(), variant);
        }
        this.variantsById = Map.copyOf(byId);
        this.transitions = List.copyOf(transitions);
        this.initial = initial;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Declare a multiplexer in one expression (mirrors the state-machine DSL): variants, transitions,
     * and the initial variant are stated inside the lambda; variant handles are captured locally so the
     * whole graph is type-safe and writable as a single declaration.
     */
    public static Multiplexer define(Consumer<Builder> config) {
        Builder builder = builder();
        config.accept(builder);
        return builder.build();
    }

    public MuxVariant initial() {
        return initial;
    }

    public MuxVariant variant(String id) {
        return variantsById.get(id);
    }

    public List<MuxVariant> variants() {
        return variants;
    }

    public List<Transition> transitions() {
        return transitions;
    }

    /** Create a fresh per-instance {@link MuxState} starting at the initial variant. */
    public MuxState newState() {
        return new MuxState(initial);
    }

    /**
     * Advance the external {@code state}: progress any in-flight cross-fade by {@code dt}, then (once
     * settled) evaluate transition guards against {@code input} and start/commit a switch.
     */
    public void advance(MuxState state, MultiplexerInput input, float dt) {
        if (state.inTransition()) {
            state.advance(dt);
            if (state.transitionDone()) {
                state.commitTransition();
            } else {
                return;
            }
        }
        for (Transition transition : transitions) {
            if (transition.from() != state.current()) {
                continue;
            }
            if (!transition.guard().test(input)) {
                continue;
            }
            if (transition.crossFadeSeconds() <= 0f) {
                state.setCurrentInstant(transition.to());
            } else {
                state.startTransition(state.current(), transition.to(), transition.crossFadeSeconds());
            }
            if (transition.onSwitch() != null) {
                transition.onSwitch().accept(state);
            }
            break;
        }
    }

    /** One directed edge: {@code from} → {@code to}, taken when {@code guard} holds. */
    public record Transition(
            MuxVariant from,
            MuxVariant to,
            Predicate<MultiplexerInput> guard,
            float crossFadeSeconds,
            Consumer<MuxState> onSwitch
    ) {

        public Transition {
            Objects.requireNonNull(from, "transition 'from' required");
            Objects.requireNonNull(to, "transition 'to' required");
            if (guard == null) {
                guard = in -> true;
            }
        }
    }

    //region builder

    public static final class Builder {

        private final List<MuxVariant> variants = new ArrayList<>();
        private final List<Transition> transitions = new ArrayList<>();
        private MuxVariant initial;

        /** Define a variant and return its typed handle (capture it to reference in transitions/initial). */
        public MuxVariant variant(String id, Consumer<MuxVariant> config) {
            MuxVariant variant = new MuxVariant(id);
            config.accept(variant);
            variants.add(variant);
            return variant;
        }

        public Builder transition(MuxVariant from, MuxVariant to, Consumer<TransitionBuilder> config) {
            TransitionBuilder builder = new TransitionBuilder(from, to);
            config.accept(builder);
            transitions.add(builder.build());
            return this;
        }

        public Builder initial(MuxVariant variant) {
            this.initial = variant;
            return this;
        }

        public Multiplexer build() {
            if (variants.isEmpty()) {
                throw new IllegalStateException("multiplexer needs at least one variant");
            }
            if (initial == null) {
                initial = variants.get(0);
            }
            return new Multiplexer(variants, transitions, initial);
        }
    }

    /** Fluent transition configurator used inside {@code transition(from, to, t -> ...)}. */
    public static final class TransitionBuilder {

        private final MuxVariant from;
        private final MuxVariant to;
        private Predicate<MultiplexerInput> guard = in -> true;
        private float crossFadeSeconds;
        private Consumer<MuxState> onSwitch;

        TransitionBuilder(MuxVariant from, MuxVariant to) {
            this.from = from;
            this.to = to;
        }

        public TransitionBuilder when(Predicate<MultiplexerInput> guard) {
            this.guard = guard;
            return this;
        }

        public TransitionBuilder crossFade(float seconds) {
            this.crossFadeSeconds = seconds;
            return this;
        }

        public TransitionBuilder onSwitch(Consumer<MuxState> callback) {
            this.onSwitch = callback;
            return this;
        }

        Transition build() {
            return new Transition(from, to, guard, crossFadeSeconds, onSwitch);
        }
    }

    //endregion
}
