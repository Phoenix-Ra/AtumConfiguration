package me.phoenixra.atumconfig.api.config;
import me.phoenixra.atumconfig.api.ConfigOwner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface ConfigManager {

    /**
     * Create config.
     *
     * @param values The values.
     * @param type   The config type.
     * @return The config
     */
    @NotNull
    Config createConfig(@Nullable Map<String, Object> values,
                        @NotNull ConfigType type);

    /**
     * Loads an existing config from the configOwner folder
     * and adds it to a configManager
     * <br><br>
     * if specified config doesn't exist
     * creates a new config with content
     * from the config owner resources
     * <br><br>
     *
     * @param name name of a config (without extension)
     * @param directory The directory of a config. Use empty if root directory
     * @param type The type of config
     * @param forceLoadResource if true - throws NullPointerException
     *                       when file not found inside the resources folder,
     *                          otherwise creates an empty file
     * @return loaded config
     */
    @NotNull
    LoadableConfig createLoadableConfig(
            @NotNull String name,
            @NotNull String directory,
            @NotNull ConfigType type,
            boolean forceLoadResource
    );

    /**
     * Reload all configs
     */
    void reloadAll();

    /**
     * Save all configs.
     */
    void saveAll();

    /**
     * Get config added to the manager
     * or null if not found
     * @param name The name of a config
     * @return this
     */
    @Nullable
    LoadableConfig getConfig(@NotNull String name);

    /**
     * Add new config to the handler
     *
     * @param config The loadable config
     * @return this
     */
    @NotNull
    ConfigManager addConfig(@NotNull LoadableConfig config);

    /**
     * Get the config owner.
     *
     * @return The config owner instance.
     */
    @NotNull
    ConfigOwner getConfigOwner();
}
