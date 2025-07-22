package me.phoenixra.atumconfig.api.config.catalog;


import me.phoenixra.atumconfig.api.config.ConfigFile;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Listener interface for receiving events
 * from configuration catalog.
 */
public interface ConfigCatalogListener {

    /**
     * Called when the catalog is about to clear all current entries.
     * <p>
     * This happens at the start of a reload or initial load, before
     * any individual config files are processed.
     *
     * @param catalog the listening catalog
     */
    void onClear(@NotNull ConfigCatalog catalog);

    /**
     * Called when a single {@link ConfigFile}
     * has been successfully loaded or reloaded.
     *
     * @param catalog the listening catalog
     * @param config the {@code ConfigFile} that was loaded
     */
    void onConfigLoaded(@NotNull ConfigCatalog catalog, @NotNull ConfigFile config);

    /**
     * Called when a config file fails to load due to an error.
     * <p>
     * Default implementation is a no-op. Override to log or handle failures.
     *
     * @param catalog the listening catalog
     * @param configPath the relative filesystem path of the config file that failed
     * @param e          the exception that occurred during load
     */
    default void onConfigFailed(@NotNull ConfigCatalog catalog, Path configPath, Throwable e) {}

    /**
     * Called immediately before the catalog begins reloading its contents.
     * <p>
     * Default implementation is a no-op. Override to prepare for reload.
     *
     * @param catalog the listening catalog
     */
    default void beforeReload(@NotNull ConfigCatalog catalog) {}

    /**
     * Called immediately after the catalog has finished reloading all contents.
     * <p>
     * Default implementation is a no-op. Override to finalize state after reload.
     *
     * @param catalog the listening catalog
     */
    default void afterReload(@NotNull ConfigCatalog catalog) {}

    /**
     * Called before the catalog started loading default resource files
     * <p>
     * Default implementation is a no-op. Override to handle default-load events.
     *
     * @param catalog the listening catalog
     */
    default void beforeLoadDefaults(@NotNull ConfigCatalog catalog) {}

    /**
     * Called after the catalog loaded default resource files
     * <p>
     * Default implementation is a no-op. Override to handle default-load events.
     *
     * @param catalog the listening catalog
     */
    default void afterLoadDefaults(@NotNull ConfigCatalog catalog) {}
}
