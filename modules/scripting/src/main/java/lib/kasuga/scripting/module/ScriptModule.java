package lib.kasuga.scripting.module;

import lib.kasuga.scripting.ScriptException;
import lib.kasuga.scripting.Tickable;

public interface ScriptModule extends Tickable {
    void init() throws ScriptException;
    void close();
}
