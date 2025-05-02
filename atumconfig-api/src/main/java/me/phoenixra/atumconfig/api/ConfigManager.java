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

import java.util.Map;
import java.util.Optional;


/**
 * Central manager for creating, loading, and maintaining configuration files
 * and catalogs for an application.
 * <p>
 * Provides methods to create standalone {@link Config} instances, load or
 * create disk-backed {@link ConfigFile}s, and group multiple config files
 * into {@link ConfigCatalog}s. Also handles registration of custom
 * {@link ConfigParser}s and integration with a {@link PlaceholderHandler}.
 */
public interface ConfigManager {

    /**
     * Gets the unique identifier for this config manager instance.
     *
     * @return non-null string ID
     */
    @NotNull
    String getId();

    /**
     * Returns the root directory on disk where config files and catalogs are stored.
     *
     * @return non-null path to the configuration directory
     */
    @NotNull
    Path getDirectory();

    /**
     * Indicates whether this manager supports Minecraft-style color codes
     * in formatted strings.
     *
     * @return true if color codes are enabled; false otherwise
     */
    boolean supportsColorCodes();

    /**
     * Reloads all {@link ConfigFile} and {@link ConfigCatalog} instances managed by this manager,
     * reflecting any external changes on disk.
     */
    void reloadAll();

    /**
     * Returns the logger used for writing informational messages,
     * warnings, and errors related to configuration operations.
     *
     * @return non-null {@link ConfigLogger}
     */
    @NotNull
    ConfigLogger getLogger();

    /**
     * Retrieves the configured placeholder handler, if present.
     *
     * @return an {@link Optional} containing the handler, or empty if none set
     */
    @NotNull
    Optional<PlaceholderHandler> getPlaceholderHandler();

    /**
     * Sets the placeholder handler to use for formatting strings
     * that may contain placeholder tokens.
     *
     * @param placeholderHandler the handler to set, or null to disable
     */
    void setPlaceholderHandler(@Nullable PlaceholderHandler placeholderHandler);

    /**
     * Retrieves a registered {@link ConfigParser} for the given domain
     * class, if available.
     *
     * @param clazz the class parsed by the desired parser
     * @param <T>   the type parsed
     * @return an Optional containing the parser if registered
     */
    @NotNull
    <T> Optional<ConfigParser<T>> getConfigParser(@NotNull Class<T> clazz);

    /**
     * Registers a {@link ConfigParser} for handling custom object serialization
     * and deserialization.
     *
     * @param configParser the parser to register, must be non-null
     */
    void addConfigParser(@NotNull ConfigParser<?> configParser);

    // =================== Config Creation ===================

    /**
     * Creates an in-memory {@link Config} of the specified type,
     * optionally pre-populated with the given key/value data.
     *
     * @param type the format (e.g. JSON, YAML)
     * @param data initial contents or null for empty
     * @return a new Config instance
     */
    @NotNull
    Config createConfig(
            @NotNull ConfigType type,
            @Nullable Map<String, Object> data
    );

    /**
     * Creates an in-memory {@link Config} by reading from the provided input stream.
     *
     * @param type the config format
     * @param in   the input stream to parse
     * @return a new Config instance
     */
    @NotNull
    Config createConfigFromStream(
            @NotNull ConfigType type,
            @NotNull InputStream in
    );

    /**
     * Creates an in-memory {@link Config} by parsing the given raw string.
     *
     * @param type the config format
     * @param raw  the raw text to parse
     * @return a new Config instance
     */
    @NotNull
    Config createConfigFromString(
            @NotNull ConfigType type,
            @NotNull String raw
    );

    // =================== Config File Creation ===================

    /**
     * Loads or creates a disk-backed {@link ConfigFile}, registering it
     * under a unique ID. If the file does not exist and {@code forceLoadResource}
     * is true, fails; otherwise creates an empty or populated from resource file.
     *
     * @param type               the config format
     * @param id                 unique identifier for later retrieval
     * @param relativePath       path under {@link #getDirectory()}
     * @param forceLoadResource  if true, require a bundled default resource
     * @return the loaded or newly created ConfigFile
     * @throws IOException on I/O or missing resource when forced
     */
    @NotNull
    ConfigFile createConfigFile(
            @NotNull ConfigType type,
            @NotNull String id,
            @NotNull Path relativePath,
            boolean forceLoadResource
    ) throws IOException;

    /**
     * Convenience overload of {@link #createConfigFile(ConfigType, String, Path, boolean)}
     * with {@code forceLoadResource} set to false.
     * @param type               the config format
     * @param id                 unique identifier for later retrieval
     * @param relativePath       path under {@link #getDirectory()}
     * @return the loaded or newly created ConfigFile
     * @throws IOException on I/O or missing resource when forced
     */
    @NotNull
    default ConfigFile createConfigFile(
            @NotNull ConfigType type,
            @NotNull String id,
            @NotNull Path relativePath
    ) throws IOException {
        return createConfigFile(type, id, relativePath, false);
    }

    // =================== Config Catalog Creation ===================

    /**
     * Creates a {@link ConfigCatalog} for grouping multiple related files
     * under a common directory. Listener methods fire during reload operations.
     *
     * @param type            the config format for all files in the catalog
     * @param id              unique catalog identifier
     * @param relativeDirectory the directory under {@link #getDirectory()}
     * @param nested          whether to traverse subdirectories
     * @param catalogListener listener for catalog events
     * @return a new ConfigCatalog instance
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
     * Convenience overload of {@link #createCatalog(ConfigType,String,Path,boolean,ConfigCatalogListener)}
     * with {@code nested} set to false.
     * @param type            the config format for all files in the catalog
     * @param id              unique catalog identifier
     * @param relativeDirectory the directory under {@link #getDirectory()}
     * @param catalogListener listener for catalog events
     * @return a new ConfigCatalog instance
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

    // =================== Configs Management ===================

    /**
     * Registers a {@link ConfigFile} instance
     *
     * @param config the config file to register
     * @return this manager for fluent chaining
     */
    @NotNull
    ConfigManager addConfigFile(@NotNull ConfigFile config);

    /**
     * Registers a {@link ConfigCatalog} instance
     *
     * @param catalog the catalog to register
     * @return this manager for fluent chaining
     */
    @NotNull
    ConfigManager addCatalog(@NotNull ConfigCatalog catalog);

    /**
     * Looks up a previously registered catalog by its unique ID.
     *
     * @param id the catalog identifier
     * @return an Optional containing the catalog if found
     */
    @NotNull
    Optional<ConfigCatalog> getCatalog(@NotNull String id);

    /**
     * Looks up a previously registered file by its unique ID.
     *
     * @param id the file identifier
     * @return an Optional containing the file if found
     */
    @NotNull
    Optional<ConfigFile> getConfigFile(@NotNull String id);

    /**
     * Returns a map of all registered config files, keyed by their IDs.
     *
     * @return non-null map of ID→ConfigFile
     */
    @NotNull
    Map<String, ConfigFile> getConfigFilesMap();

    /**
     * Returns a map of all registered config catalogs, keyed by their IDs.
     *
     * @return non-null map of ID→ConfigCatalog
     */
    @NotNull
    Map<String, ConfigCatalog> getConfigCatalogsMap();
}

