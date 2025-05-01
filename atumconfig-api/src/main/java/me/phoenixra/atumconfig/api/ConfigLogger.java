package me.phoenixra.atumconfig.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Abstraction for logging within the configuration system.
 * <p>
 * Custom implementation can be provided to integrate configuration
 * with any logging framework.
 * </p>
 */
public interface ConfigLogger {

    ConfigLogger EMPTY = new ConfigLogger() {
        @Override
        public void logInfo(@NotNull String msg) {}
        @Override
        public void logWarn(@NotNull String msg) {}
        @Override
        public void logError(@NotNull String msg) {}
    };

    ConfigLogger SIMPLE = new ConfigLogger() {
        @Override
        public void logInfo(@NotNull String msg) {System.out.println("INFO: "+msg);}
        @Override
        public void logWarn(@NotNull String msg) {System.out.println("WARN: "+msg);}
        @Override
        public void logError(@NotNull String msg) {System.out.println("ERROR: "+msg);}
    };

    /**
     * Logs an informational message.
     *
     * @param msg the message to log, must be non-null
     */
    void logInfo(@NotNull String msg);

    /**
     * Logs a warning message.
     *
     * @param msg the message to log, must be non-null
     */
    void logWarn(@NotNull String msg);

    /**
     * Logs an error message.
     *
     * @param msg the message to log, must be non-null
     */
    void logError(@NotNull String msg);

    /**
     * Logs an error message with optional exception details.
     *
     * @param msg the error message, may be null
     * @param t   the throwable whose stack trace to log
     */
    default void logError(@Nullable String msg, @NotNull Throwable t) {
        if (msg != null) {
            logError(msg);
        }
        for (StackTraceElement element : t.getStackTrace()) {
            logError(element.toString());
        }
        Throwable cause = t.getCause();
        if (cause != null) {
            logError("Caused by: " + cause);
        }
        for (Throwable suppressed : t.getSuppressed()) {
            logError(null, suppressed);
        }
    }
}
