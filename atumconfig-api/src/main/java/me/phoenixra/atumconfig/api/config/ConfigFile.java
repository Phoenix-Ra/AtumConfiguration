package me.phoenixra.atumconfig.api.config;


import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Represents a configuration file that is both backed by disk storage and
 * managed in-memory via the {@link Config}
 * <p>
 * Extends {@link Config} to provide file-specific operations
 */
public interface ConfigFile extends Config {


    /**
     * Creates the underlying file on disk if it does not already exist.
     * <p>
     * If {@code forceResourceLoad} is true and no bundled resource is found
     * for this file, an exception will be thrown.
     *
     * @param forceResourceLoad whether to require a bundled resource if the file is missing
     */
    void createFile(boolean forceResourceLoad);

    /**
     * Reloads this configuration file from disk, refreshing all values
     * in memory to match the current file contents.
     *
     * @throws IOException if an error occurs reading the file
     */
    void reload() throws IOException;

    /**
     * Saves the in-memory configuration values back to disk, writing all
     * current key/value pairs to the file in the appropriate format.
     *
     * @throws IOException if an error occurs writing the file
     */
    void save() throws IOException;

    /**
     * Returns the path to this file, relative to the configuration root
     * directory managed by the {@link me.phoenixra.atumconfig.api.ConfigManager}.
     *
     * @return non-null relative file path
     */
    @NotNull
    Path getRelativeFilePath();

    /**
     * Returns the absolute {@link File} object for this configuration file.
     *
     * @return non-null file instance
     */
    @NotNull
    File getFile();

    /**
     * Returns the name of the file (including extension).
     *
     * @return non-null file name string
     */
    @NotNull
    default String getFileName() {
        return getFile().getName();
    }

    /**
     * Returns the logical name of this configuration, derived from the
     * file name without its extension.
     *
     * @return non-null config name (filename without extension)
     */
    default @NotNull String getName() {
        return getFileName().replace("." + getType().getFileExtension(), "");
    }

    /**
     * Returns the resource path within the application JAR (or classpath)
     * from which this file may be loaded as a default.
     *
     * @return non-null classpath resource path
     */
    @NotNull
    String getResourcePath();

    /**
     * Returns the unique identifier under which this file is registered
     * in the {@link me.phoenixra.atumconfig.api.ConfigManager}.
     *
     * @return non-null config file ID
     */
    @NotNull
    String getId();
}
