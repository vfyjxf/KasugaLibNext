package lib.kasuga.rendering.models.uml.dynamic.fsm;

/**
 * Pilot factory: builds a small reference-style machine for an owner. Generic (uses trigger +
 * whenComplete, not owner fields) so it works for any {@link Owner}. A living example of the DSL.
 */
public final class FsmPilots {

    private FsmPilots() {}

    public static <Owner> StateMachine<Owner> demo(Owner owner) {
        return StateMachine.<Owner>builder(owner)
                .layer("base", layer -> {
                    State<Owner> idle = layer.state("idle");
                    State<Owner> active = layer.state("active").durationTicks(2);
                    layer.initial(idle);
                    layer.transition("start", idle, active).on("activate");
                    layer.transition("loop", active, idle).whenComplete();
                })
                .build();
    }
}
