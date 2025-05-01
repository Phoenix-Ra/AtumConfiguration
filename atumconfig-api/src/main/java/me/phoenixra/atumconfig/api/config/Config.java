package me.phoenixra.atumconfig.api.config;

import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.api.placeholders.context.PlaceholderList;
import me.phoenixra.atumconfig.api.placeholders.context.PlaceholderContext;
import me.phoenixra.atumconfig.api.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public interface Config extends PlaceholderList {

    /**
     * Convert the config into text
     * used in writing config
     * data into a file
     *
     * @return The plaintext.
     */
    String toPlaintext();

    /**
     * Get if the config contains a specified path
     *
     * @param path The path to check.
     * @return true if contains
     */
    boolean hasPath(@NotNull String path);

    /**
     * Get config keys.
     *
     * @param deep If keys from subsections should be added to that list as well
     * @return A list of the keys.
     */
    @NotNull
    List<String> getKeys(boolean deep);

    /**
     * Recurse config keys
     *
     * @param found The found keys.
     * @param root  The root.
     * @return The found keys.
     */
    @NotNull
    default List<String> recurseKeys(@NotNull Set<String> found,
                                     @NotNull String root) {
        return new ArrayList<>();
    }

    /**
     * Apply data to config.
     * <br>
     * It will remove the current data.
     * @param values the map to apply
     */
    void applyData(Map<String, Object> values);

    /**
     * Apply data to config from different config
     * <br>
     * It will remove the current data.
     * @param config the config to get data from
     */
    default void applyData(Config config){
        applyData(config.toMap());
    }

    /**
     * Get an object in config
     *
     * @param path The path.
     * @return The object or null if not found.
     */
    @Nullable
    Object get(@NotNull String path);

    /**
     * Set an object in config
     * <br>
     * Set null to remove the config section
     * <br>
     * You can also set a {@link Config} object, so it will be a subsection
     * <br>
     * If you want to set it to config file as well,
     * use {@link ConfigFile#save()}
     *
     * @param path The path.
     * @param obj  The object.
     */
    void set(@NotNull String path,
             @Nullable Object obj);

    /**
     * Get the byte
     *
     * @param path The path.
     * @param def  The default value.
     * @return The byte or default value if not found.
     */
    default <T> T getParsedOrDefault(@NotNull String path,
                                   Class<T> clazz,
                                   T def) {
        return Objects.requireNonNullElse(
                getParsedOrNull(path,clazz), def
        );
    }

    /**
     * Get the byte
     *
     * @param path The path.
     * @return The byte or null if not found.
     */
    @Nullable
    <T> T getParsedOrNull(@NotNull String path, Class<T> clazz);

    /**
     * Get the byte
     *
     * @param path The path.
     * @return The byte or 0 if not found.
     **/
    default byte getByte(@NotNull String path) {
        return Objects.requireNonNullElse(getByteOrNull(path), (byte)0);
    }

    /**
     * Get the byte
     *
     * @param path The path.
     * @param def  The default value.
     * @return The byte or default value if not found.
     */
    default byte getByteOrDefault(@NotNull String path,
                                  byte def) {
        return Objects.requireNonNullElse(getByteOrNull(path), def);
    }

    /**
     * Get the byte
     *
     * @param path The path.
     * @return The byte or null if not found.
     */
    @Nullable
    Byte getByteOrNull(@NotNull String path);

    /**
     * Get the byte list
     *
     * @param path The path.
     * @return The byte list or empty list if not found.
     */
    @NotNull
    default List<Byte> getByteList(@NotNull String path) {
        return Objects.requireNonNullElse(getByteListOrNull(path), new ArrayList<>());
    }

    /**
     * Get the byte list
     *
     * @param path The path.
     * @return The byte list or null if not found.
     */
    @Nullable
    List<Byte> getByteListOrNull(@NotNull String path);

    /**
     * Get the short
     *
     * @param path The path.
     * @return The short or 0 if not found.
     **/
    default short getShort(@NotNull String path) {
        return Objects.requireNonNullElse(getShortOrNull(path), (short)0);
    }

    /**
     * Get the short
     *
     * @param path The path.
     * @param def  The default value.
     * @return The short or default value if not found.
     */
    default short getShortOrDefault(@NotNull String path,
                                    short def) {
        return Objects.requireNonNullElse(getShortOrNull(path), def);
    }

    /**
     * Get the short
     *
     * @param path The path.
     * @return The short or null if not found.
     */
    @Nullable
    Short getShortOrNull(@NotNull String path);

    /**
     * Get the short list
     *
     * @param path The path.
     * @return The short list or empty list if not found.
     */
    @NotNull
    default List<Short> getShortList(@NotNull String path) {
        return Objects.requireNonNullElse(getShortListOrNull(path), new ArrayList<>());
    }

    /**
     * Get the short list
     *
     * @param path The path.
     * @return The short list or null if not found.
     */
    @Nullable
    List<Short> getShortListOrNull(@NotNull String path);

    /**
     * Get the int
     *
     * @param path The path.
     * @return The int or 0 if not found.
     **/
    default int getInt(@NotNull String path) {
        return Objects.requireNonNullElse(getIntOrNull(path), 0);
    }

    /**
     * Get the int
     *
     * @param path The path.
     * @param def  The default value.
     * @return The int or default value if not found.
     */
    default int getIntOrDefault(@NotNull String path,
                                int def) {
        return Objects.requireNonNullElse(getIntOrNull(path), def);
    }

    /**
     * Get the int
     *
     * @param path The path.
     * @return The int or null if not found.
     */
    @Nullable
    Integer getIntOrNull(@NotNull String path);

    /**
     * Get the int list
     *
     * @param path The path.
     * @return The int list or empty list if not found.
     */
    @NotNull
    default List<Integer> getIntList(@NotNull String path) {
        return Objects.requireNonNullElse(getIntListOrNull(path), new ArrayList<>());
    }

    /**
     * Get the int list
     *
     * @param path The path.
     * @return The int list or null if not found.
     */
    @Nullable
    List<Integer> getIntListOrNull(@NotNull String path);

    /**
     * Get the long
     *
     * @param path The path.
     * @return The long or 0 if not found.
     **/
    default long getLong(@NotNull String path) {
        return Objects.requireNonNullElse(getLongOrNull(path), 0L);
    }

    /**
     * Get the long
     *
     * @param path The path.
     * @param def  The default value.
     * @return The long or default value if not found.
     */
    default long getLongOrDefault(@NotNull String path,
                                 long def) {
        return Objects.requireNonNullElse(getLongOrNull(path), def);
    }

    /**
     * Get the long
     *
     * @param path The path.
     * @return The long or null if not found.
     */
    @Nullable
    Long getLongOrNull(@NotNull String path);

    /**
     * Get the long list
     *
     * @param path The path.
     * @return The long list or empty list if not found.
     */
    @NotNull
    default List<Long> getLongList(@NotNull String path) {
        return Objects.requireNonNullElse(getLongListOrNull(path), new ArrayList<>());
    }

    /**
     * Get the long list
     *
     * @param path The path.
     * @return The long list or null if not found.
     */
    @Nullable
    List<Long> getLongListOrNull(@NotNull String path);

    /**
     * Get the float
     *
     * @param path The path.
     * @return The float or 0 if not found.
     */
    default float getFloat(@NotNull String path) {
        return Objects.requireNonNullElse(getFloatOrNull(path), 0.0f);
    }

    /**
     * Get the float
     *
     * @param path The path.
     * @param def  The default value.
     * @return The float or default value if not found.
     */
    default float getFloatOrDefault(@NotNull String path, float def) {
        return Objects.requireNonNullElse(getFloatOrNull(path), def);
    }

    /**
     * Get the float
     *
     * @param path The path.
     * @return The float or null if not found.
     */
    @Nullable
    Float getFloatOrNull(@NotNull String path);

    /**
     * Get the float list
     *
     * @param path The path.
     * @return The float list or empty list if not found.
     */
    @NotNull
    default List<Float> getFloatList(@NotNull String path) {
        return Objects.requireNonNullElse(getFloatListOrNull(path), new ArrayList<>());
    }

    /**
     * Get the float list
     *
     * @param path The path.
     * @return The float list or null if not found.
     */
    @Nullable
    List<Float> getFloatListOrNull(@NotNull String path);

    /**
     * Get the double
     *
     * @param path The path.
     * @return The double or 0 if not found.
     */
    default double getDouble(@NotNull String path) {
        return Objects.requireNonNullElse(getDoubleOrNull(path), 0.0);
    }

    /**
     * Get the double
     *
     * @param path The path.
     * @param def  The default value.
     * @return The double or default value if not found.
     */
    default double getDoubleOrDefault(@NotNull String path, double def) {
        return Objects.requireNonNullElse(getDoubleOrNull(path), def);
    }

    /**
     * Get the double
     *
     * @param path The path.
     * @return The double or null if not found.
     */
    @Nullable
    Double getDoubleOrNull(@NotNull String path);

    /**
     * Get the double list
     *
     * @param path The path.
     * @return The double list or empty list if not found.
     */
    @NotNull
    default List<Double> getDoubleList(@NotNull String path) {
        return Objects.requireNonNullElse(getDoubleListOrNull(path), new ArrayList<>());
    }

    /**
     * Get the double list
     *
     * @param path The path.
     * @return The double list or null if not found.
     */
    @Nullable
    List<Double> getDoubleListOrNull(@NotNull String path);

    /**
     * Get the boolean
     *
     * @param path The path.
     * @return The boolean or false if not found.
     */
    default boolean getBool(@NotNull String path) {
        return Objects.requireNonNullElse(getBoolOrNull(path), false);
    }

    /**
     * Get the boolean list
     *
     * @param path The path.
     * @return The boolean list or null list if not found.
     */
    @Nullable
    Boolean getBoolOrNull(@NotNull String path);

    /**
     * Get the boolean list
     *
     * @param path The path.
     * @return The boolean list or empty list if not found.
     */
    @NotNull
    default List<Boolean> getBoolList(@NotNull String path) {
        return Objects.requireNonNullElse(getBoolListOrNull(path), new ArrayList<>());
    }

    /**
     * Get the boolean list
     *
     * @param path The path.
     * @return The boolean list or null if not found.
     */
    @Nullable
    List<Boolean> getBoolListOrNull(@NotNull String path);


    /**
     * Get the string
     *
     * @param path The path.
     * @return The string or empty string if not found.
     */
    @NotNull
    default String getString(@NotNull String path) {
        return getStringOrDefault(path, "");
    }

    /**
     * Get the string
     *
     * @param path The path.
     * @param def  The default value.
     * @return The string or default value if not found.
     */
    @NotNull
    default String getStringOrDefault(@NotNull String path, @NotNull String def) {
        return Objects.requireNonNullElse(getStringOrNull(path), def);
    }

    /**
     * Get the string
     *
     * @param path The path.
     * @return The string or null if not found.
     */
    @Nullable
    String getStringOrNull(@NotNull String path);

    /**
     * Get the formatted string
     * <br><br>
     * The string will be formatted
     * with colors(if mc support enabled) and global placeholders
     *
     * @param path The path.
     * @return The formatted string or empty string if not found.
     */
    @NotNull
    default String getFormattedString(@NotNull String path) {
        return Objects.requireNonNullElse(getFormattedStringOrNull(path,null), "");
    }

    /**
     * Get the formatted string
     * <br><br>
     * The string will be formatted
     * with colors(if mc support enabled) and placeholders
     *
     * @param path The path.
     * @param context The placeholder context.
     * @return The formatted string or empty string if not found.
     */
    @NotNull
    default String getFormattedString(@NotNull String path,
                                      @Nullable PlaceholderContext context) {
        return Objects.requireNonNullElse(getFormattedStringOrNull(path,context), "");
    }

    /**
     * Get the formatted string
     * <br><br>
     * The string will be formatted
     * with colors(if mc support enabled) and global placeholders
     *
     * @param path The path.
     * @return The formatted string or null if not found.
     */
    @Nullable
    default String getFormattedStringOrNull(@NotNull String path){
        return getFormattedString(path,null);
    }

    /**
     * Get the formatted string
     * <br><br>
     * The string will be formatted
     * with the placeholders and colors(if mc support enabled)
     *
     * @param path The path.
     * @param context The placeholder context.
     * @return The formatted string or null if not found.
     */
    @Nullable
    default String getFormattedStringOrNull(@NotNull String path,
                                            @Nullable PlaceholderContext context){
        String text = getStringOrNull(path);
        if(text == null) return null;
        return StringUtils.formatWithPlaceholders(
                getConfigOwner(),
                getConfigOwner().supportsColorCodes()?
                        StringUtils.formatMinecraftColors(text) : text,
                context != null ? context.withContext(this) :
                        new PlaceholderContext(this)
        );
    }

    /**
     * Get the string list
     *
     * @param path The path.
     * @return The string list or empty list if not found.
     */
    @NotNull
    default List<String> getStringList(@NotNull String path) {
        return Objects.requireNonNullElse(getStringListOrNull(path), new ArrayList<>());
    }

    /**
     * Get the string list
     *
     * @param path The path.
     * @return The string list or null if not found.
     */
    @Nullable
    List<String> getStringListOrNull(@NotNull String path);

    /**
     * Get the formatted string list
     * <br><br>
     * The string list will be formatted
     * with colors(if mc support enabled) and global placeholders
     *
     * @param path The path.
     * @return The formatted string list or empty list if not found.
     */
    @NotNull
    default List<String> getFormattedStringList(@NotNull String path) {
        return Objects.requireNonNullElse(getFormattedStringListOrNull(path,null), new ArrayList<>());
    }

    /**
     * Get the formatted string list
     * <br><br>
     * The string list will be formatted
     * with the placeholders and colors(if mc support enabled)
     *
     * @param path The path.
     * @param context The placeholder context.
     * @return The formatted string list or empty list if not found.
     */
    @NotNull
    default List<String> getFormattedStringList(@NotNull String path,
                                                @Nullable PlaceholderContext context) {
        return Objects.requireNonNullElse(getFormattedStringListOrNull(path,context), new ArrayList<>());
    }

    /**
     * Get the formatted string list
     * <br><br>
     * The string list will be formatted
     * with colors(if mc support enabled) and global placeholders
     *
     * @param path The path.
     * @return The formatted string list or null if not found.
     */
    @Nullable
    default List<String> getFormattedStringListOrNull(@NotNull String path) {
        return getFormattedStringListOrNull(path,null);
    }

    /**
     * Get the formatted string list
     * <br><br>
     * The string list will be formatted
     * with the placeholders and colors(if mc support enabled)
     *
     * @param path The path.
     * @param context The placeholder context.
     * @return The formatted string list or null if not found.
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
                            StringUtils.formatMinecraftColors(list) : list,
                    new PlaceholderContext(this)
            );
        }
        return StringUtils.formatWithPlaceholders(
                getConfigOwner(),
                getConfigOwner().supportsColorCodes()?
                        StringUtils.formatMinecraftColors(list) : list,
                context.withContext(this)
        );
    }

    /**
     * Get the AtumConfig subsection
     * <br><br>
     * Works the same way as FileConfiguration#getConfigurationSection
     *
     * @param path  The path.
     * @return The config, or empty {@link Config} if not found.
     */
    @NotNull
    default Config getSubsection(@NotNull String path){
        return Objects.requireNonNullElse(
                getSubsectionOrNull(path),
                getConfigOwner().createConfig(
                        getType(),
                        null
                )
        );
    }

    /**
     * Get the AtumConfig subsection
     * <br><br>
     * Works the same way as FileConfiguration#getConfigurationSection
     *
     * @param path  The path.
     * @return The config, or null if not found.
     */
    @Nullable
    Config getSubsectionOrNull(@NotNull String path);

    /**
     * Get all subsections of the key
     *
     * @param path  The path.
     * @return The found config list, or empty {@link ArrayList} if not found.
     */
    @NotNull
    default List<? extends Config> getSubsectionList(@NotNull String path) {
        return Objects.requireNonNullElse(getSubsectionListOrNull(path), new ArrayList<>());
    }

    /**
     * Get all subsections of the key
     *
     * @param path  The path.
     * @return The found value, or null
     */
    @Nullable
    List<? extends Config> getSubsectionListOrNull(@NotNull String path);

    /**
     * Get the evaluated expression
     * from string
     *
     * @param path The path.
     * @return The evaluated value.
     */
    default double getEvaluated(@NotNull String path){
        return getEvaluated(path, PlaceholderContext.EMPTY);
    }

    /**
     * Get the evaluated expression
     * from string
     *
     * @param path The path.
     * @param context The placeholder context.
     * @return The evaluated value.
     */
    double getEvaluated(@NotNull String path,
                        @NotNull PlaceholderContext context);

    /**
     * Get the config values as a map
     *
     * @return The map of the config values.
     */
    default Map<String, Object> toMap() {
        return new HashMap<>();
    }

    /**
     * Get the config type
     *
     * @return The config type
     */
    @NotNull ConfigType getType();

    /**
     * Get the owner of a config
     *
     * @return The config owner instance.
     */
    ConfigManager getConfigOwner();

}
