package me.phoenixra.atumconfig.api;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.atumconfig.api.config.ConfigParser;
import me.phoenixra.atumconfig.api.config.ConfigType;
import me.phoenixra.atumconfig.api.config.catalog.ConfigCatalog;
import me.phoenixra.atumconfig.api.config.catalog.ConfigCatalogListener;
import me.phoenixra.atumconfig.api.placeholders.PlaceholderHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Manages configuration files and catalogs for an application.
 * Provides methods to create, load, reload, and retrieve configurations
 */
public interface ConfigManager {

    /**
     * Returns the unique identifier for this config manager.
     *
     * @return non-null unique identifier
     */
    @NotNull
    String getId();

    /**
     * Returns the directory where configuration files are stored.
     *
     * @return non-null path to the config directory
     */
    @NotNull
    Path getDirectory();

    /**
     * Indicates whether this manager supports Minecraft-style color codes
     * in text formatting.
     *
     * @return {@code true} if color codes are supported; {@code false} otherwise
     */
    boolean supportsColorCodes();


    /**
     * Reloads all managed configuration files, refreshing their contents
     * from disk
     */
    void reloadAll();

    /**
     * Returns the logger used by this configuration manager.
     *
     * @return non-null {@link ConfigLogger} instance
     */
    @NotNull
    ConfigLogger getLogger();

    /**
     * Returns the placeholders handler used by this configuration manager.
     *
     * @return an {@link Optional} containing the placeholder handler if found, otherwise empty
     */
    @NotNull
    Optional<PlaceholderHandler> getPlaceholderHandler();

    void setPlaceholderHandler(@Nullable PlaceholderHandler placeholderHandler);


    @NotNull
    <T> Optional<ConfigParser<T>> getConfigParser(@NotNull Class<T> clazz);

    void addConfigParser(@NotNull ConfigParser<?> configParser);

    /*=================== Config Creation ===================*/

    /**
     * Creates a standalone {@link Config} instance with the given data.
     * This does not attach the config to any file.
     *
     * @param type the type of configuration to create
     * @param data an optional map of key-value pairs for initial content.
     *            If NULL -> empty config is created
     * @return a non-null {@link Config} instance
     */
    @NotNull
    Config createConfig(
            @NotNull ConfigType type,
            @Nullable Map<String, Object> data
    );

    /**
     * Creates a standalone {@link Config} by reading data from an input stream.
     * This does not attach the config to any file.
     *
     * @param type the type of configuration to create
     * @param in   the input stream to read the configuration from
     * @return a non-null {@link Config} instance
     */
    @NotNull
    Config createConfigFromStream(
            @NotNull ConfigType type,
            @NotNull InputStream in
    );

    /**
     * Creates a standalone {@link Config} from a raw string.
     * This does not attach the config to any file
     *
     * @param type the type of configuration to create
     * @param raw  the raw string containing configuration data
     * @return a non-null {@link Config} instance
     */
    @NotNull
    Config createConfigFromString(
            @NotNull ConfigType type,
            @NotNull String raw
    );

    /*=================== Config File Creation ===================*/

    /**
     * Loads or creates a configuration file and registers it with this manager.
     * <p>If the file exists in {@code getDirectory()}, loads it. Otherwise, creates from resources:</p>
     * <ul>
     *     <li>If {@code forceLoadResource} is {@code true}, throws
     *         {@link IOException} when the resource is missing.</li>
     *     <li>If {@code false}, creates an empty file or one populated from the
     *         bundled resources.</li>
     * </ul>
     *
     * @param type                      the type of configuration
     * @param id                        the config identifier for manager storage
     * @param relativePath              path to file relative to {@link #getDirectory()}
     * @param forceLoadResource         if {@code true}, fail if bundled resource is missing
     * @return a non-null {@link ConfigFile} instance
     * @throws IOException if an I/O error occurs creating or loading the file
     */
    @NotNull
    ConfigFile createConfigFile(
            @NotNull ConfigType type,
            @NotNull String id,
            @NotNull Path relativePath,
            boolean forceLoadResource
    ) throws IOException;

    /**
     * Overload of {@link #createConfigFile(ConfigType, String, Path, boolean)}
     * with {@code forceLoadResource} set to {@code false}.
     *
     * @param type           the type of configuration
     * @param id             the config identifier for manager storage
     * @param relativePath   relativePath to file relative to {@link #getDirectory()}
     * @return a non-null {@link ConfigFile} instance
     * @throws IOException if an I/O error occurs creating or loading the file
     */
    @NotNull
    default ConfigFile createConfigFile(
            @NotNull ConfigType type,
            @NotNull String id,
            @NotNull Path relativePath
    ) throws IOException{
        return createConfigFile(type, id, relativePath, false);
    }


    /*=================== Config Catalog Creation ===================*/

    /**
     * Creates a new {@link ConfigCatalog} with the provided parameters
     * and registers it with this manager.
     *
     * @param type                the type of configurations in this catalog
     * @param id                  the unique identifier for the catalog
     * @param relativeDirectory   path to catalog directory relative to {@link #getDirectory()}
     * @param nested              whether nested directories are supported
     * @param catalogListener     listener to catalog events
     * @return a non-null {@link ConfigCatalog} instance
     */
    @NotNull
    ConfigCatalog createCatalog(
            @NotNull ConfigType type,
            @NotNull String id,
            @NotNull Path relativeDirectory,
            boolean nested,
            @NotNull ConfigCatalogListener catalogListener
    );

    /**
     * Overload of {@link #createCatalog(ConfigType, String, Path, boolean, ConfigCatalogListener)}
     * with {@code nested} set to {@code false}.
     *
     * @param type                the type of configurations in this catalog
     * @param id                  the unique identifier for the catalog
     * @param relativeDirectory   path to catalog directory relative to {@link #getDirectory()}
     * @param catalogListener     listener to catalog events
     * @return a non-null {@link ConfigCatalog} instance
     */
    @NotNull
    default ConfigCatalog createCatalog(
            @NotNull ConfigType type,
            @NotNull String id,
            @NotNull Path relativeDirectory,
            @NotNull ConfigCatalogListener catalogListener
    ) {
        return createCatalog(type, id, relativeDirectory, false, catalogListener);
    }

    /*=================== Configs Management ===================*/

    /**
     * Registers a {@link ConfigFile} with this manager.
     *
     * @param config the config file to add
     * @return this manager instance
     */
    @NotNull
    ConfigManager addConfigFile(@NotNull ConfigFile config);

    /**
     * Registers a {@link ConfigCatalog} with this manager.
     *
     * @param catalog the config catalog to add
     * @return this manager instance
     */
    @NotNull
    ConfigManager addCatalog(@NotNull ConfigCatalog catalog);

    /**
     * Retrieves a registered {@link ConfigCatalog} by its identifier.
     *
     * @param id the identifier of the catalog
     * @return an {@link Optional} containing the catalog if found, otherwise empty
     */
    @NotNull
    Optional<ConfigCatalog> getCatalog(@NotNull String id);

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
     * Retrieves all registered {@link ConfigCatalog}
     *
     * @return map
     */
    @NotNull
    Map<String, ConfigCatalog> getConfigCatalogsMap();




}
