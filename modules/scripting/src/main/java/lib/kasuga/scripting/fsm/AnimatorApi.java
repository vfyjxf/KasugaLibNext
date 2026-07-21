package lib.kasuga.scripting.fsm;

import lib.kasuga.rendering.models.uml.dynamic.fsm.StateMachine;
import lib.kasuga.rendering.models.uml.dynamic.fsm.Layer;
import lib.kasuga.rendering.models.uml.dynamic.fsm.MachineRegistry;
import lib.kasuga.scripting.security.Api;

/**
 * Script-facing control surface for {@link StateMachine}s — a polyglot-neutral API keyed by
 * a long handle (from {@link MachineRegistry#register}). Registered as the engine global "Animator"
 * via {@link FsmApiRegistration#install}. Scripts: {@code Animator.trigger(handle, "attack")},
 * {@code Animator.goTo(handle, "upper_body", "attack.windup")}.
 */
public final class AnimatorApi {

    private final MachineRegistry registry;

    public AnimatorApi() {
        this(MachineRegistry.GLOBAL);
    }

    public AnimatorApi(MachineRegistry registry) {
        this.registry = registry;
    }

    private StateMachine<?> machine(long handle) {
        return registry.resolve(handle);
    }

    @Api
    public void trigger(long handle, String name) {
        StateMachine<?> m = machine(handle);
        if (m != null) m.trigger(name);
    }

    @Api
    public void goTo(long handle, String layerId, String stateId) {
        StateMachine<?> m = machine(handle);
        if (m != null) m.goTo(layerId, stateId);
    }

    @Api
    public String getState(long handle, String layerId) {
        StateMachine<?> m = machine(handle);
        if (m == null) return "";
        Layer<?> layer = m.layerOrNull(layerId);
        return (layer == null || layer.active() == null) ? "" : layer.active().id();
    }

    @Api
    public void signal(long handle, String name, Object value) {
        StateMachine<?> m = machine(handle);
        if (m != null) m.setSignal(name, value);
    }

    @Api
    public Object signal(long handle, String name) {
        StateMachine<?> m = machine(handle);
        return m == null ? null : m.signal(name);
    }
}
