package me.phoenixra.atumconfig.api.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Converts between domain objects of type {@code T} and their {@link Config} representation.
 * <p>
 * Implementations are responsible for serializing an instance of {@code T}
 * into a provided empty {@code Config}, and for deserializing a new instance
 * of {@code T} from a populated {@code Config}.
 *
 * @param <T> the type this parser handles
 */
public interface ConfigParser<T> {

    /**
     * Serializes the given value into the provided empty config.
     * <p>
     * Implementations should populate all necessary fields on {@code emptyConfig}
     * based on the properties of {@code value}. If {@code value} is not an instance
     * of the expected type, or cannot be converted, this method should return {@code null}.
     *
     * @param value       the object to serialize (expected to be of type {@code T})
     * @param emptyConfig a fresh {@link Config} instance to populate
     * @return the populated {@code Config}, or {@code null} if conversion is not applicable
     */
    @Nullable
    Config toConfig(Object value, Config emptyConfig);

    /**
     * Deserializes an instance of {@code T} from the given config.
     * <p>
     * Implementations should read all required values from {@code config}
     * and construct a new instance of {@code T}. If the config is missing required
     * keys or contains invalid data, this method should return {@code null}.
     *
     * @param config the {@link Config} containing serialized data
     * @return a new instance of {@code T}, or {@code null} if deserialization fails
     */
    @Nullable
    T fromConfig(Config config);

    /**
     * Returns the {@link Class} object for the type parsed by this parser.
     * @return the {@code Class<T>} this parser handles
     */
    @NotNull
    Class<T> getClassParsed();

}
