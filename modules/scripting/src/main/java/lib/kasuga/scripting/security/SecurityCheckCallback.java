package lib.kasuga.scripting.security;

import lib.kasuga.scripting.ScriptEngine;

@FunctionalInterface
public interface SecurityCheckCallback {
    boolean check(ScriptEngine engine, Api.Parameters[] parameters);
}
