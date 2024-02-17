package me.phoenixra.atumconfig.api.config.serialization;


import me.phoenixra.atumconfig.api.ConfigOwner;
import me.phoenixra.atumconfig.api.config.Config;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Load objects from configs
 *
 * @param <T> The type of object to load
 */
public interface ConfigDeserializer<T> {

    /**
     * Load an object from config
     *
     * @param configOwner the config owner
     * @param config The config.
     * @return The object.
     */
    @Nullable
    T deserializeFromConfig(@NotNull ConfigOwner configOwner, @NotNull Config config);
}
