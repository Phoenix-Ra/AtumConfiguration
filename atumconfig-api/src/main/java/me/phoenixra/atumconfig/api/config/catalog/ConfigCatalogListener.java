package me.phoenixra.atumconfig.api.config.catalog;

import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;


/**
 * Listener for catalog events and individual config items as they’re loaded.
 */
public interface ConfigCatalogListener {

    /** Clear any stored data before a fresh load. */
    void onClear();

    /**
     * Called for each config file in the catalog as it’s parsed.
     *
     * @param config   the parsed {@link ConfigFile} instance
     */
    void onConfigLoaded(@NotNull ConfigFile config);

    /** Invoked when config loading thrown an error */
    default void onConfigFailed(Path configPath, Throwable e) {}

    /** Invoked before the catalog is reloaded. */
    default void beforeReload() {}

    /** Invoked after the catalog has been reloaded. */
    default void afterReload() {}

    /** Invoked after defaults loaded */
    default void onLoadDefaults() {}
}
