package me.phoenixra.atumconfig.api;

import me.phoenixra.atumconfig.api.config.ConfigManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public interface ConfigOwner {

    @NotNull
    ConfigManager getConfigManager();


    @NotNull
    Logger getLogger();

    @NotNull
    File getDataFolder();

    @NotNull
    String getName();

    boolean supportMinecraft();

    /**
     * Log info to console
     *
     * @param message the message
     */
    void logInfo(String message);

    /**
     * Log warning to console
     *
     * @param message the message
     */
    void logWarning(String message);

    /**
     * Log error to console
     *
     * @param message the message
     */
    void logError(String message);

}
