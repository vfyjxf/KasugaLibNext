package lib.kasuga.scripting.fsm;

import lib.kasuga.scripting.security.Api;

/**
 * RESERVED stub: the script-side mirror of {@code StateMachineBuilder} (build {@code StateMachineDefinition}s
 * from JS object literals). The surface is declared now so the engine global "AnimatorBuilder" exists;
 * the implementation is deferred (it feeds {@code ScriptDefinitionSource}).
 */
public final class AnimatorBuilderApi {

    @Api
    public String status() {
        return "AnimatorBuilderApi is reserved (script-declared FSM definitions) — implementation deferred.";
    }
}
