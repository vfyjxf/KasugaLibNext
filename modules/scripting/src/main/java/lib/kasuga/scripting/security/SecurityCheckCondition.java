package lib.kasuga.scripting.security;

import lib.kasuga.scripting.ScriptEngine;

public interface SecurityCheckCondition {
    boolean test(ScriptEngine engine, Api.Parameters[] parameters);
}
