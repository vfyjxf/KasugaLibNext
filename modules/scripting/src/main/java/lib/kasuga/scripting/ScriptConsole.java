package lib.kasuga.scripting;

import org.slf4j.Logger;

public interface ScriptConsole {
    void log(String message);
    void warn(String message);
    void debug(String message);
    void info(String message);
    void error(String message);
}
