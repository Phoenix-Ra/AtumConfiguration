package me.phoenixra.atumconfig.api.config;

import me.phoenixra.atumconfig.api.ConfigOwner;
import me.phoenixra.atumconfig.api.placeholders.InjectablePlaceholderList;
import me.phoenixra.atumconfig.api.placeholders.context.PlaceholderContext;
import me.phoenixra.atumconfig.api.utils.StringUtils;
import me.phoenixra.atumconfig.utils.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public interface Config extends InjectablePlaceholderList {

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
     * use {@link LoadableConfig#save()}
     *
     * @param path The path.
     * @param obj  The object.
     */
    void set(@NotNull String path,
             @Nullable Object obj);

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
                getConfigOwner().supportMinecraft()?
                        StringUtils.formatMinecraftColors(text) : text,
                context != null ? context.withInjectableContext(this) :
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
                    getConfigOwner().supportMinecraft()?
                            StringUtils.formatMinecraftColors(list) : list,
                    new PlaceholderContext(this)
            );
        }
        return StringUtils.formatWithPlaceholders(
                getConfigOwner(),
                getConfigOwner().supportMinecraft()?
                        StringUtils.formatMinecraftColors(list) : list,
                context.withInjectableContext(this)
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
                getConfigOwner().getConfigManager().createConfig(
                        null,
                        getType()
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
    ConfigOwner getConfigOwner();



}
