package lib.kasuga.scripting;

import org.slf4j.Logger;

public interface ScriptConsole {
    void log(String message);
    void warn(String message);
    void debug(String message);
    void info(String message);
    void error(String message);

    /**
     * A console that discards all output.
     */
    static ScriptConsole noop() {
        return NoopScriptConsole.INSTANCE;
    }

    /**
     * A console that delegates all output to the given SLF4J logger.
     */
    static ScriptConsole slf4j(Logger logger) {
        return new Slf4jScriptConsole(logger);
    }

    /**
     * A console that suppresses log/warn/debug/info but routes error to System.err.
     * Useful for tests.
     */
    static ScriptConsole errorsToStderr() {
        return ErrorsOnlyScriptConsole.INSTANCE;
    }
}

class NoopScriptConsole implements ScriptConsole {
    static final NoopScriptConsole INSTANCE = new NoopScriptConsole();
    @Override public void log(String message) {}
    @Override public void warn(String message) {}
    @Override public void debug(String message) {}
    @Override public void info(String message) {}
    @Override public void error(String message) {}
}

class Slf4jScriptConsole implements ScriptConsole {
    private final Logger logger;

    Slf4jScriptConsole(Logger logger) {
        this.logger = logger;
    }

    @Override public void log(String message) { logger.info(message); }
    @Override public void warn(String message) { logger.warn(message); }
    @Override public void debug(String message) { logger.debug(message); }
    @Override public void info(String message) { logger.info(message); }
    @Override public void error(String message) { logger.error(message); }
}

class ErrorsOnlyScriptConsole implements ScriptConsole {
    static final ErrorsOnlyScriptConsole INSTANCE = new ErrorsOnlyScriptConsole();
    @Override public void log(String message) {}
    @Override public void warn(String message) {}
    @Override public void debug(String message) {}
    @Override public void info(String message) {}
    @Override public void error(String message) { System.err.println("[ERROR] " + message); }
}
