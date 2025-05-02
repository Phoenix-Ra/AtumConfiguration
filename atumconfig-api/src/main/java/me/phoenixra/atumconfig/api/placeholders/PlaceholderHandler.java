package me.phoenixra.atumconfig.api.placeholders;

import me.phoenixra.atumconfig.api.ConfigLogger;
import me.phoenixra.atumconfig.api.placeholders.context.PlaceholderContext;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Responsible for registering placeholders and translating placeholder tokens
 * within text strings. Placeholders are denoted by percent signs (e.g. {@code %name%}).
 * <p>
 * Handlers may maintain a set of global placeholders and apply them to any text passed
 * to the {@code translatePlaceholders} methods. Context-specific placeholders can be
 * provided via a {@link PlaceholderContext}.
 */
public interface PlaceholderHandler {

    /**
     * Regular expression to detect placeholders of the form {@code %key%},
     * where {@code key} is any sequence of characters except percent or space.
     */
    Pattern PATTERN = Pattern.compile("%([^% ]+)%");

    /**
     * A no-op placeholder handler that performs no substitutions and holds no placeholders.
     */
    PlaceholderHandler EMPTY = new PlaceholderHandler() {
        @Override public void unregisterGlobalPlaceholder(@NotNull Placeholder placeholder) {}
        @Override public void registerGlobalPlaceholder(@NotNull Placeholder placeholder) {}
        @Override public @NotNull Set<Placeholder> getGlobalPlaceholders() { return Collections.emptySet();}
        @Override public @NotNull String translatePlaceholders(@NotNull String text) { return text; }
        @Override public @NotNull String translatePlaceholders(@NotNull String text, @NotNull PlaceholderContext context) { return text; }
        @Override public @NotNull ConfigLogger getLogger() { return ConfigLogger.EMPTY; }
    };

    /**
     * Registers a placeholder in the global scope. Once registered, this placeholder
     * will be applied to all texts processed by {@code translatePlaceholders}.
     *
     * @param placeholder the placeholder to register, must be non-null
     */
    void registerGlobalPlaceholder(@NotNull Placeholder placeholder);

    /**
     * Unregisters a previously registered global placeholder.
     * If the placeholder is not registered, this method does nothing.
     *
     * @param placeholder the placeholder to remove, must be non-null
     */
    void unregisterGlobalPlaceholder(@NotNull Placeholder placeholder);

    /**
     * Retrieves all currently registered global placeholders.
     *
     * @return a non-null set of global placeholders
     */
    @NotNull
    Set<Placeholder> getGlobalPlaceholders();

    /**
     * Translates all placeholder tokens in the given text using only the global
     * placeholders. This is equivalent to calling
     * {@link #translatePlaceholders(String, PlaceholderContext)} with an empty context.
     *
     * @param text the input text containing zero or more placeholder tokens
     * @return the text with all placeholders replaced by their mapped values
     */
    @NotNull
    String translatePlaceholders(@NotNull String text);

    /**
     * Translates all placeholder tokens in the given text, first applying global
     * placeholders, then applying placeholders available in the given context.
     * Context placeholders override global ones when keys conflict.
     *
     * @param text    the input text containing placeholder tokens
     * @param context the placeholder context providing additional or overriding placeholders
     * @return the text with all placeholders replaced by their mapped values
     */
    @NotNull
    String translatePlaceholders(@NotNull String text,
                                 @NotNull PlaceholderContext context);

    /**
     * Returns the {@link ConfigLogger} used for logging messages, warnings,
     * or errors encountered during placeholder processing.
     *
     * @return non-null ConfigLogger instance
     */
    @NotNull
    ConfigLogger getLogger();

    /**
     * Finds all unique placeholder tokens in the input text. A placeholder token
     * matches the {@link #PATTERN} regex and includes the surrounding percent signs.
     *
     * @param text the text to scan for placeholders
     * @return a list of distinct placeholder strings (including percent signs), or
     *         an empty list if none found
     */
    @NotNull
    static List<String> findPlaceholdersIn(@NotNull String text) {
        Set<String> found = new HashSet<>();
        Matcher matcher = PATTERN.matcher(text);
        while (matcher.find()) {
            found.add(matcher.group());
        }
        return new ArrayList<>(found);
    }

}
