package me.phoenixra.atumconfig.api;

import me.phoenixra.atumconfig.api.config.ConfigManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface ConfigOwner {

    /**
     * Get Configuration Manager
     *
     * @return the config manager
     */
    @NotNull
    ConfigManager getConfigManager();

    /**
     * Get Data Folder
     *
     * @return the data folder
     */
    @NotNull
    File getDataFolder();

    /**
     * Get config owner name
     *
     * @return the name
     */
    @NotNull
    String getName();

    /**
     * If supports minecraft
     *
     * @return true/false
     */
    boolean supportMinecraft();

    /**
     * Log info to console
     *
     * @param message the message
     */
    void logInfo(@NotNull String message);

    /**
     * Log warning to console
     *
     * @param message the message
     */
    void logWarning(@NotNull String message);

    /**
     * Log error to console
     *
     * @param message the message
     */
    void logError(@NotNull String message);


    default void logError(@Nullable String message, @NotNull Throwable throwable){
        if(message != null){
            logError(message);
        }
        for (StackTraceElement s : throwable.getStackTrace()) {
            logError(s.toString());
        }
        if(throwable.getCause() != null) {
            logError("Caused by:");
            logError(throwable.getCause().toString());
        }
        for(Throwable err : throwable.getSuppressed()){
            logError(null, err);
        }
    }

}
