package me.phoenixra.atumconfig.api.placeholders.types;

import me.phoenixra.atumconfig.api.placeholders.Placeholder;
import me.phoenixra.atumconfig.api.placeholders.context.PlaceholderContext;
import me.phoenixra.atumconfig.api.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * A placeholder that always returns a fixed value when its identifier is encountered.
 * <p>
 * The identifier is a literal string wrapped in percent signs (e.g. <code>%key%</code>).
 * Upon translation, the identifier is replaced by the supplied value or {@code null} to replace with empty
 */
public final class StaticPlaceholder implements Placeholder {

    private final String identifier;


    private final Pattern pattern;


    private final Supplier<@Nullable String> function;


    /**
     * Constructs a static placeholder with a fixed identifier and supplier.
     *
     * @param identifier the placeholder name (without percent signs)
     * @param function   supplies the replacement text each time the placeholder is found or {@code null} to replace with empty
     */
    public StaticPlaceholder(@NotNull final String identifier,
                             @NotNull final Supplier<@Nullable String> function) {
        this.identifier = "%" + identifier + "%";
        this.pattern = Pattern.compile(this.identifier, Pattern.LITERAL);
        this.function = function;
    }

    @Override
    public @Nullable String getValue(@NotNull final String replacing,
                                     @NotNull final PlaceholderContext context) {
        return function.get();
    }

    @Override
    public String tryTranslateQuickly(@NotNull final String text,
                                      @NotNull final PlaceholderContext context) {
        return StringUtils.replaceFast(
                text,
                this.identifier,
                Objects.requireNonNullElse(this.getValue(this.identifier, context), "")
        );
    }

    @Override
    public String toString() {
        return "StaticPlaceholder[identifier=" + this.identifier + "]";
    }

    @NotNull
    @Override
    public Pattern getPattern() {
        return this.pattern;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StaticPlaceholder)) {
            return false;
        }
        StaticPlaceholder that = (StaticPlaceholder) o;
        return Objects.equals(this.getPattern(), that.getPattern());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getPattern());
    }
}
