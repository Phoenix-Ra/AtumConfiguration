package me.phoenixra.atumconfig.api.placeholders;

import me.phoenixra.atumconfig.api.placeholders.context.PlaceholderContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Placeholder
 */
public interface Placeholder {

    /**
     * Get the value of the placeholder
     *
     * @param replacing    Replacing text
     * @param context      The context.
     * @return The result
     */
    @Nullable
    String getValue(@NotNull String replacing,
                    @NotNull PlaceholderContext context);

    /**
     * Get the pattern for the placeholder
     *
     * @return The pattern.
     */
    @NotNull
    Pattern getPattern();

    /**
     * Try to translate all instances of this placeholder in text quickly.
     *
     * @param text    The text to translate.
     * @param context The context.
     * @return The translated text.
     */
    default String tryTranslateQuickly(@NotNull final String text,
                                       @NotNull final PlaceholderContext context) {
        return text;
    }

}
