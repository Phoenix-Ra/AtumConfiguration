package me.phoenixra.atumconfig.api;

import me.phoenixra.atumconfig.api.config.ConfigManager;
import org.jetbrains.annotations.NotNull;

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
