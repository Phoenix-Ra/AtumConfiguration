package me.phoenixra.atumconfig.api.config.catalog;

import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.atumconfig.api.config.ConfigType;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a collection of configuration
 * files of a specific type.
 */
public interface ConfigCatalog {

    /**
     * Returns the manager that owns this catalog.
     *
     * @return a non-null {@link ConfigManager}
     */
    @NotNull
    ConfigManager getConfigManager();

    /**
     * Returns the type of configurations in this catalog.
     *
     * @return a non-null {@link ConfigType}
     */
    @NotNull
    ConfigType getType();

    /**
     * Returns the unique identifier of this catalog.
     *
     * @return a non-null catalog id
     */
    @NotNull
    String getId();



    /**
     * Returns the directory (relative to the manager's root) containing this catalog's files.
     *
     * @return a non-null {@link Path}
     */
    @NotNull
    Path getDirectory();

    /**
     * Indicates whether nested subdirectories are supported in this catalog.
     *
     * @return {@code true} if nested directories are allowed; {@code false} otherwise
     */
    boolean isNestedDirectories();

    /**
     * Retrieves a registered {@link ConfigFile} by its id.
     *
     * @param id the id.
     * @return an {@link Optional} containing the config file if found, otherwise empty
     */
    @NotNull
    Optional<ConfigFile> getConfigFile(@NotNull String id);


    /**
     * Retrieves all registered {@link ConfigFile}
     *
     * @return map
     */
    @NotNull
    Map<String, ConfigFile> getConfigFilesMap();

    /**
     * Returns the listener of catalog events
     *
     * @return a non-null {@link ConfigCatalogListener}
     */
    @NotNull
    ConfigCatalogListener getListener();


    /**
     * Reloads all configuration files in this catalog from disk or resources.
     */
    void reload();
}
