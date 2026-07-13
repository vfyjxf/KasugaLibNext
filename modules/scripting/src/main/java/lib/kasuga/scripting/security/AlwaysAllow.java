package lib.kasuga.scripting.security;

import lib.kasuga.scripting.ScriptEngine;

public class AlwaysAllow implements SecurityCheckCondition {
    @Override
    public boolean test(ScriptEngine engine, Api.Parameters[] parameters) {
        return true;
    }
}
