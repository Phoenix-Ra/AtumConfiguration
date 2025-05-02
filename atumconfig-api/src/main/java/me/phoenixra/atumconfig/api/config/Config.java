package me.phoenixra.atumconfig.api.config;

import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.api.placeholders.context.PlaceholderList;
import me.phoenixra.atumconfig.api.placeholders.context.PlaceholderContext;
import me.phoenixra.atumconfig.api.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.phoenixra.atumconfig.api.utils.Objects;
import java.util.*;

/**
 * Represents a hierarchical, in-memory configuration object that can be serialized
 * to and deserialized from disk-backed files (via {@link ConfigFile}) or
 * loaded from raw strings/streams.  Supports nested sections (sub-configs),
 * placeholder injection, and type-safe getters for primitives, lists, and custom
 * objects via {@link ConfigParser}s.
 * <p>
 * Config instances are created by a {@link ConfigManager} and carry knowledge of their
 * owning manager (for parser lookup and placeholder handling) and their format type
 * ({@link #getType()}).
 */
public interface Config extends PlaceholderList {


    /**
     * Returns the {@link ConfigManager} that created and owns this config,
     * which provides access to parsers, placeholder handling, and file operations.
     *
     * @return non-null ConfigManager responsible for this config
     */
    ConfigManager getConfigOwner();

    /**
     * Returns the format type of this configuration (e.g., JSON or YAML),
     *
     * @return non-null ConfigType indicating the underlying format
     */
    @NotNull ConfigType getType();

    /**
     * Produces a raw map representation of this config tree, suitable for
     * programmatic inspection or serialization. Nested configs and lists
     * are represented as maps and lists, respectively.
     *
     * @return non-null map of key→value mappings
     */
    default Map<String, Object> toMap() {
        return new HashMap<>();
    }

    /**
     * Serializes this config into its plaintext representation in the
     * underlying format (JSON, YAML, etc.), preserving key order and
     * formatting nested objects and lists appropriately.
     *
     * @return non-null plaintext serialization
     */
    String toPlaintext();

    /**
     * Checks whether a value exists at the given dotted path (e.g. "a.b.c").
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return true if a non-null value exists at the path
     */
    boolean hasPath(@NotNull String path);

    /**
     * Retrieves the immediate top-level keys in this config.
     *
     * @param deep if true, recursively include nested subsection keys (prefixed)
     * @return list of key names
     */
    @NotNull
    List<String> getKeys(boolean deep);

    /**
     * Recursively accumulates keys into a flat list, prefixing them by {@code root}.
     * Used internally for deep key listing.
     *
     * @param found internally-used set to avoid cycles
     * @param root  current key prefix
     * @return list of fully-qualified keys
     */
    @NotNull
    default List<String> recurseKeys(@NotNull Set<String> found,
                                     @NotNull String root) {
        return Collections.emptyList();
    }

    /**
     * Returns a map of all immediate subsections in this config, keyed
     * by their direct field names, preserving insertion order.
     *
     * @return non-null ordered map of subsection name → {@link Config}
     */
    @NotNull Map<String, Config> getAllSubsections();

    /**
     * Overwrites this config's data with the provided map, clearing existing values.
     * Nested maps or sections will be converted via the manager's type handlers.
     *
     * @param values non-null key→value map of primitives, lists, or nested maps
     */
    void applyData(@NotNull Map<String, Object> values);

    /**
     * Copies data from another {@code Config} into this one.
     *
     * @param config source config to shallow-copy
     */
    default void applyData(Config config) {
        applyData(config.toMap());
    }

    /**
     * Retrieves the raw Java object stored at the given path, which may be
     * a primitive wrapper, List, or nested {@code Config}.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return stored object or null
     */
    @Nullable
    Object get(@NotNull String path);

    /**
     * Sets a value at the given path.  Supports nested assignment for
     * dot-separated paths, creating subsections as needed.  Passing {@code null}
     * removes the key.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @param obj  value to set or null to remove
     */
    void set(@NotNull String path,
             @Nullable Object obj);


    // =================== Getters for Primitives ===================


    /**
     * Retrieves a {@code byte} value at the given path, or {@code 0} if the path
     * is missing or the stored value is not numeric.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return the primitive byte value, or {@code 0} when missing or invalid
     */
    default byte getByte(@NotNull String path) {
        return Objects.requireNonNullElse(getByteOrNull(path), (byte)0);
    }

    /**
     * Retrieves a {@code byte} value at the given path, or returns the supplied
     * default if the path is missing or the stored value cannot be converted.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @param def  default byte to return when missing or invalid
     * @return the byte value, or {@code def} on missing or invalid data
     */
    default byte getByteOrDefault(@NotNull String path,
                                  byte def) {
        return Objects.requireNonNullElse(getByteOrNull(path), def);
    }

    /**
     * Retrieves a boxed {@link Byte} at the specified path.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return a {@code Byte} if present and numeric, otherwise {@code null}
     */
    @Nullable
    Byte getByteOrNull(@NotNull String path);

    /**
     * Retrieves a list of {@link Byte} values at the specified path.
     * If the path does not exist or is not a list of numeric values,
     * returns an empty list.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return non-null {@code List<Byte>} (possibly empty)
     */
    @NotNull
    default List<Byte> getByteList(@NotNull String path) {
        return Objects.requireNonNullElse(getByteListOrNull(path), new ArrayList<>());
    }

    /**
     * Retrieves a list of {@link Byte} values at the specified path,
     * or {@code null} if the path is missing or not a list of numeric values.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return {@code List<Byte>} or {@code null}
     */
    @Nullable
    List<Byte> getByteListOrNull(@NotNull String path);


    /**
     * Retrieves a {@code short} value at the given path, or {@code 0} if the path
     * is missing or the stored value is not numeric.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return the primitive short value, or {@code 0} when missing or invalid
     */
    default short getShort(@NotNull String path) {
        return Objects.requireNonNullElse(getShortOrNull(path), (short)0);
    }

    /**
     * Retrieves a {@code short} value at the given path, or returns the supplied
     * default if the path is missing or the stored value cannot be converted.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @param def  default short to return when missing or invalid
     * @return the short value, or {@code def} on missing or invalid data
     */
    default short getShortOrDefault(@NotNull String path,
                                    short def) {
        return Objects.requireNonNullElse(getShortOrNull(path), def);
    }

    /**
     * Retrieves a boxed {@link Short} at the specified path.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return a {@code Short} if present and numeric, otherwise {@code null}
     */
    @Nullable
    Short getShortOrNull(@NotNull String path);

    /**
     * Retrieves a list of {@link Short} values at the specified path.
     * If the path does not exist or is not a list of numeric values,
     * returns an empty list.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return non-null {@code List<Short>} (possibly empty)
     */
    @NotNull
    default List<Short> getShortList(@NotNull String path) {
        return Objects.requireNonNullElse(getShortListOrNull(path), new ArrayList<>());
    }

    /**
     * Retrieves a list of {@link Short} values at the specified path,
     * or {@code null} if the path is missing or not a list of numeric values.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return {@code List<Short>} or {@code null}
     */
    @Nullable
    List<Short> getShortListOrNull(@NotNull String path);


    /**
     * Retrieves an {@code int} value at the given path, or {@code 0} if the path
     * is missing or the stored value is not numeric.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return the primitive int value, or {@code 0} when missing or invalid
     */
    default int getInt(@NotNull String path) {
        return Objects.requireNonNullElse(getIntOrNull(path), 0);
    }

    /**
     * Retrieves an {@code int} value at the given path, or returns the supplied
     * default if the path is missing or the stored value cannot be converted.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @param def  default int to return when missing or invalid
     * @return the int value, or {@code def} on missing or invalid data
     */
    default int getIntOrDefault(@NotNull String path,
                                int def) {
        return Objects.requireNonNullElse(getIntOrNull(path), def);
    }

    /**
     * Retrieves a boxed {@link Integer} at the specified path.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return an {@code Integer} if present and numeric, otherwise {@code null}
     */
    @Nullable
    Integer getIntOrNull(@NotNull String path);

    /**
     * Retrieves a list of {@link Integer} values at the specified path.
     * If the path does not exist or is not a list of numeric values,
     * returns an empty list.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return non-null {@code List<Integer>} (possibly empty)
     */
    @NotNull
    default List<Integer> getIntList(@NotNull String path) {
        return Objects.requireNonNullElse(getIntListOrNull(path), new ArrayList<>());
    }

    /**
     * Retrieves a list of {@link Integer} values at the specified path,
     * or {@code null} if the path is missing or not a list of numeric values.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return {@code List<Integer>} or {@code null}
     */
    @Nullable
    List<Integer> getIntListOrNull(@NotNull String path);


    /**
     * Retrieves a {@code long} value at the given path, or {@code 0L} if the path
     * is missing or the stored value is not numeric.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return the primitive long value, or {@code 0L} when missing or invalid
     */
    default long getLong(@NotNull String path) {
        return Objects.requireNonNullElse(getLongOrNull(path), 0L);
    }

    /**
     * Retrieves a {@code long} value at the given path, or returns the supplied
     * default if the path is missing or the stored value cannot be converted.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @param def  default long to return when missing or invalid
     * @return the long value, or {@code def} on missing or invalid data
     */
    default long getLongOrDefault(@NotNull String path,
                                 long def) {
        return Objects.requireNonNullElse(getLongOrNull(path), def);
    }

    /**
     * Retrieves a boxed {@link Long} at the specified path.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return a {@code Long} if present and numeric, otherwise {@code null}
     */
    @Nullable
    Long getLongOrNull(@NotNull String path);

    /**
     * Retrieves a list of {@link Long} values at the specified path.
     * If the path does not exist or is not a list of numeric values,
     * returns an empty list.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return non-null {@code List<Long>} (possibly empty)
     */
    @NotNull
    default List<Long> getLongList(@NotNull String path) {
        return Objects.requireNonNullElse(getLongListOrNull(path), new ArrayList<>());
    }

    /**
     * Retrieves a list of {@link Long} values at the specified path,
     * or {@code null} if the path is missing or not a list of numeric values.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return {@code List<Long>} or {@code null}
     */
    @Nullable
    List<Long> getLongListOrNull(@NotNull String path);


    /**
     * Retrieves a {@code float} value at the given path, or {@code 0.0f} if the path
     * is missing or the stored value is not numeric.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return the primitive float value, or {@code 0.0f} when missing or invalid
     */
    default float getFloat(@NotNull String path) {
        return Objects.requireNonNullElse(getFloatOrNull(path), 0.0f);
    }

    /**
     * Retrieves a {@code float} value at the given path, or returns the supplied
     * default if the path is missing or the stored value cannot be converted.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @param def  default float to return when missing or invalid
     * @return the float value, or {@code def} on missing or invalid data
     */
    default float getFloatOrDefault(@NotNull String path, float def) {
        return Objects.requireNonNullElse(getFloatOrNull(path), def);
    }

    /**
     * Retrieves a boxed {@link Float} at the specified path.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return a {@code Float} if present and numeric, otherwise {@code null}
     */
    @Nullable
    Float getFloatOrNull(@NotNull String path);

    /**
     * Retrieves a list of {@link Float} values at the specified path.
     * If the path does not exist or is not a list of numeric values,
     * returns an empty list.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return non-null {@code List<Float>} (possibly empty)
     */
    @NotNull
    default List<Float> getFloatList(@NotNull String path) {
        return Objects.requireNonNullElse(getFloatListOrNull(path), new ArrayList<>());
    }

    /**
     * Retrieves a list of {@link Float} values at the specified path,
     * or {@code null} if the path is missing or not a list of numeric values.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return {@code List<Float>} or {@code null}
     */
    @Nullable
    List<Float> getFloatListOrNull(@NotNull String path);


    /**
     * Retrieves a {@code double} value at the given path, or {@code 0.0} if the path
     * is missing or the stored value is not numeric.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return the primitive double value, or {@code 0.0} when missing or invalid
     */
    default double getDouble(@NotNull String path) {
        return Objects.requireNonNullElse(getDoubleOrNull(path), 0.0);
    }

    /**
     * Retrieves a {@code double} value at the given path, or returns the supplied
     * default if the path is missing or the stored value cannot be converted.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @param def  default double to return when missing or invalid
     * @return the double value, or {@code def} on missing or invalid data
     */
    default double getDoubleOrDefault(@NotNull String path, double def) {
        return Objects.requireNonNullElse(getDoubleOrNull(path), def);
    }

    /**
     * Retrieves a boxed {@link Double} at the specified path.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return a {@code Double} if present and numeric, otherwise {@code null}
     */
    @Nullable
    Double getDoubleOrNull(@NotNull String path);

    /**
     * Retrieves a list of {@link Double} values at the specified path.
     * If the path does not exist or is not a list of numeric values,
     * returns an empty list.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return non-null {@code List<Double>} (possibly empty)
     */
    @NotNull
    default List<Double> getDoubleList(@NotNull String path) {
        return Objects.requireNonNullElse(getDoubleListOrNull(path), new ArrayList<>());
    }

    /**
     * Retrieves a list of {@link Double} values at the specified path,
     * or {@code null} if the path is missing or not a list of numeric values.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return {@code List<Double>} or {@code null}
     */
    @Nullable
    List<Double> getDoubleListOrNull(@NotNull String path);


    /**
     * Retrieves a {@code boolean} value at the given path, or {@code false}
     * if the path is missing or the stored value is not a boolean.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return the primitive boolean value, or {@code false} when missing or invalid
     */
    default boolean getBool(@NotNull String path) {
        return Objects.requireNonNullElse(getBoolOrNull(path), false);
    }

    /**
     * Retrieves a {@code boolean} value at the given path, or returns the supplied
     * default if the path is missing or the stored value cannot be converted.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @param def  default boolean to return when missing or invalid
     * @return the boolean value, or {@code def} on missing or invalid data
     */
    default boolean getBoolOrDefault(@NotNull String path, boolean def) {
        return Objects.requireNonNullElse(getBoolOrNull(path), def);
    }

    /**
     * Retrieves a boxed {@link Boolean} at the specified path.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return a {@code Boolean} if present and boolean, otherwise {@code null}
     */
    @Nullable
    Boolean getBoolOrNull(@NotNull String path);

    /**
     * Retrieves a list of {@link Boolean} values at the specified path.
     * If the path does not exist or is not a list of boolean values,
     * returns an empty list.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return non-null {@code List<Boolean>} (possibly empty)
     */
    @NotNull
    default List<Boolean> getBoolList(@NotNull String path) {
        return Objects.requireNonNullElse(getBoolListOrNull(path), new ArrayList<>());
    }

    /**
     * Retrieves a list of {@link Boolean} values at the specified path,
     * or {@code null} if the path is missing or not a list of boolean values.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return {@code List<Boolean>} or {@code null}
     */
    @Nullable
    List<Boolean> getBoolListOrNull(@NotNull String path);



    // =================== Getters for Strings ===================



    /**
     * Retrieves a non-null {@link String} value at the given path.
     * If the path is missing or the stored value is null, returns an empty string.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return the string value, or "" when missing/null
     */
    @NotNull
    default String getString(@NotNull String path) {
        return getStringOrDefault(path, "");
    }

    /**
     * Retrieves a non-null {@link String} at the given path, or returns the
     * supplied default if the path is missing or the stored value is null.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @param def  default string to return when missing/null
     * @return the string value, or {@code def} when missing/null
     */
    @NotNull
    default String getStringOrDefault(@NotNull String path, @NotNull String def) {
        return Objects.requireNonNullElse(getStringOrNull(path), def);
    }

    /**
     * Retrieves a boxed {@link String} at the specified path.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return the string value, or {@code null} if missing
     */
    @Nullable
    String getStringOrNull(@NotNull String path);

    /**
     * Retrieves a list of {@link String} values at the specified path.
     * If the path does not exist or is not a list, returns an empty list.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return non-null {@code List<String>} (possibly empty)
     */
    @NotNull
    default List<String> getStringList(@NotNull String path) {
        return Objects.requireNonNullElse(getStringListOrNull(path), new ArrayList<>());
    }

    /**
     * Retrieves a list of {@link String} values at the specified path,
     * or {@code null} if the path is missing or not a list.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return {@code List<String>} or {@code null}
     */
    @Nullable
    List<String> getStringListOrNull(@NotNull String path);


    /**
     * Retrieves and formats the string at the given path, applying global
     * placeholders and color codes (if supported). Returns an empty string
     * if the value is missing or null.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return the formatted string, or "" when missing/null
     */
    @NotNull
    default String getFormattedString(@NotNull String path) {
        return Objects.requireNonNullElse(getFormattedStringOrNull(path,null), "");
    }

    /**
     * Retrieves and formats the string at the given path using the provided
     * placeholder context. Returns an empty string if missing or null.
     *
     * @param path    dot-delimited key path (e.g. "section.key")
     * @param context placeholder context for formatting (may be null)
     * @return the formatted string, or "" when missing/null
     */
    @NotNull
    default String getFormattedString(@NotNull String path,
                                      @Nullable PlaceholderContext context) {
        return Objects.requireNonNullElse(getFormattedStringOrNull(path,context), "");
    }

    /**
     * Retrieves and formats the string at the given path, or returns null
     * if missing. Uses an empty placeholder context.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return the formatted string, or null when missing
     */
    @Nullable
    default String getFormattedStringOrNull(@NotNull String path){
        return getFormattedString(path,null);
    }

    /**
     * Retrieves and formats the string at the given path with the specified
     * placeholder context. Returns null if missing.
     *
     * @param path    dot-delimited key path (e.g. "section.key")
     * @param context placeholder context (null for default)
     * @return the formatted string, or null when missing
     */
    @Nullable
    default String getFormattedStringOrNull(@NotNull String path,
                                            @Nullable PlaceholderContext context){
        String text = getStringOrNull(path);
        if(text == null) return null;
        return StringUtils.formatWithPlaceholders(
                getConfigOwner(),
                getConfigOwner().supportsColorCodes()?
                        StringUtils.formatColorCodes(text) : text,
                context != null ? context.withContext(this) :
                        new PlaceholderContext(this)
        );
    }

    /**
     * Retrieves and formats each string in the list at the given path,
     * applying global placeholders and color codes. Returns an empty list
     * if missing or not a list.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return non-null list of formatted strings
     */
    @NotNull
    default List<String> getFormattedStringList(@NotNull String path) {
        return Objects.requireNonNullElse(getFormattedStringListOrNull(path,null), new ArrayList<>());
    }

    /**
     * Retrieves and formats each string in the list at the given path using
     * the provided placeholder context. Returns an empty list if missing.
     *
     * @param path    dot-delimited key path (e.g. "section.key")
     * @param context placeholder context, or null for default
     * @return non-null list of formatted strings
     */
    @NotNull
    default List<String> getFormattedStringList(@NotNull String path,
                                                @Nullable PlaceholderContext context) {
        return Objects.requireNonNullElse(getFormattedStringListOrNull(path,context), new ArrayList<>());
    }

    /**
     * Retrieves and formats each string in the list at the given path, or
     * returns null if missing or not a list.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return list of formatted strings, or null
     */
    @Nullable
    default List<String> getFormattedStringListOrNull(@NotNull String path) {
        return getFormattedStringListOrNull(path,null);
    }

    /**
     * Retrieves and formats each string in the list at the given path using
     * the provided placeholder context, or returns null if missing.
     *
     * @param path    dot-delimited key path (e.g. "section.key")
     * @param context placeholder context, or null for default
     * @return list of formatted strings, or null
     */
    @Nullable
    default List<String> getFormattedStringListOrNull(@NotNull String path,
                                                      @Nullable PlaceholderContext context){
        List<String> list = getStringListOrNull(path);
        if(list == null) return null;
        if(context == null){
            return StringUtils.formatWithPlaceholders(
                    getConfigOwner(),
                    getConfigOwner().supportsColorCodes()?
                            StringUtils.formatColorCodes(list) : list,
                    new PlaceholderContext(this)
            );
        }
        return StringUtils.formatWithPlaceholders(
                getConfigOwner(),
                getConfigOwner().supportsColorCodes()?
                        StringUtils.formatColorCodes(list) : list,
                context.withContext(this)
        );
    }



    // =================== Special Getters ===================



    /**
     * Retrieves a nested subsection at the given path, or creates an empty one
     * if no subsection exists. Created subsections will be of the same type
     * and key order behavior as this config.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return existing or newly created {@link Config} subsection (never null)
     */
    @NotNull
    default Config getSubsection(@NotNull String path){
        return Objects.requireNonNullElse(
                getSubsectionOrNull(path),
                getConfigOwner().createConfig(
                        getType(), null
                )
        );
    }

    /**
     * Retrieves a nested subsection at the given path, if present.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return the {@link Config} subsection, or null if none exists or value is not a subsection
     */
    @Nullable
    Config getSubsectionOrNull(@NotNull String path);

    /**
     * Retrieves a list of subsections at the given path. If the path
     * does not exist or is not a list of subsections, returns an empty list.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return non-null list of {@link Config} subsections (possibly empty)
     */
    @NotNull
    default List<? extends Config> getSubsectionList(@NotNull String path) {
        return Objects.requireNonNullElse(getSubsectionListOrNull(path), new ArrayList<>());
    }

    /**
     * Retrieves a list of subsections at the given path, if present.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return list of {@link Config} subsections, or null if none exists or value is not a list
     */
    @Nullable
    List<? extends Config> getSubsectionListOrNull(@NotNull String path);


    /**
     * Retrieves an object of type {@code T} by delegating to a registered
     * {@link ConfigParser}. Returns {@code def} if the parser is absent or the
     * section is missing.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @param clazz class of the desired object
     * @param def default value if missing or unparseable
     * @param <T>  target type
     * @return parsed instance or the provided default
     */
    default <T> T getParsedOrDefault(@NotNull String path,
                                     Class<T> clazz,
                                     T def) {
        return Objects.requireNonNullElse(
                getParsedOrNull(path,clazz), def
        );
    }

    /**
     * Retrieves an object of type {@code T} by delegating to a registered
     * {@link ConfigParser}. Returns null if the parser is absent or section missing.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @param clazz class of the desired object
     * @param <T>  target type
     * @return parsed instance or null
     */
    @Nullable
    <T> T getParsedOrNull(@NotNull String path, Class<T> clazz);

    /**
     * Retrieves a list of {@code T} by parsing each element in a list section with
     * a registered {@link ConfigParser}. Returns empty list if missing.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @param clazz element type
     * @param <T>  element type
     * @return list of parsed elements (never null)
     */
    @NotNull
    default<T> List<T> getParsedList(@NotNull String path, Class<T> clazz) {
        return Objects.requireNonNullElse(getParsedListOrNull(path, clazz), new ArrayList<>());
    }

    /**
     * Retrieves a list of {@code T} by parsing each element in a list section with
     * a registered {@link ConfigParser}. Returns null if parser missing or section invalid.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @param clazz element type
     * @param <T>  element type
     * @return parsed list or null
     */
    @Nullable
    <T> List<T> getParsedListOrNull(@NotNull String path, Class<T> clazz);


    /**
     * Evaluates an arithmetic expression stored as a string at the given path.
     * Placeholders within the string are processed using an empty context.
     *
     * @param path dot-delimited key path (e.g. "section.key")
     * @return evaluated numeric result, or 0.0 when missing/invalid
     */
    default double getEvaluated(@NotNull String path){
        return getEvaluated(path, PlaceholderContext.EMPTY);
    }

    /**
     * Evaluates an arithmetic expression stored as a string at the given path,
     * after applying placeholders from the provided context.
     *
     * @param path    dot-delimited key path (e.g. "section.key")
     * @param context placeholder context to inject values before evaluation
     * @return evaluated numeric result, or 0.0 when missing/invalid
     */
    double getEvaluated(@NotNull String path,
                        @NotNull PlaceholderContext context);


}
