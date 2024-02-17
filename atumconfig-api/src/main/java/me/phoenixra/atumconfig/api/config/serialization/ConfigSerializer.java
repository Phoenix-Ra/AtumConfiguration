package me.phoenixra.atumconfig.api.config.serialization;

import me.phoenixra.atumconfig.api.ConfigOwner;
import me.phoenixra.atumconfig.api.config.Config;
import org.jetbrains.annotations.NotNull;

/**
 * Save objects to configs.
 *
 * @param <T> The type of object to save
 */
public interface ConfigSerializer<T> {

    /**
     * Save an object to a config.
     * <p>Use AtumAPI#getInstance#createConfig</p>
     *
     * @param configOwner the config owner
     * @param obj The object.
     * @return The config.
     */
    @NotNull
    Config serializeToConfig(@NotNull ConfigOwner configOwner, @NotNull T obj);
}
