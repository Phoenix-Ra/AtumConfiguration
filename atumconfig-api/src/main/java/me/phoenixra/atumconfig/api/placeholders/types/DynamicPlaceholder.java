package me.phoenixra.atumconfig.api.placeholders.types;

import me.phoenixra.atumconfig.api.placeholders.Placeholder;
import me.phoenixra.atumconfig.api.placeholders.context.PlaceholderContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;


/**
 * A placeholder that evaluates its value dynamically based on a regex match.
 * <p>
 * The placeholder is defined by a regex encapsulated in percent signs (e.g. <code>%key:123%</code>),
 * where <code>innerPattern</code> defines what is allowed inside the percent signs.
 *
 * The provided function is invoked with the full matched token (including percent signs)
 * and should return the replacement string or {@code null} to replace with empty
 *
 * @see #getPattern()
 */
public class DynamicPlaceholder implements Placeholder {

    private final Pattern pattern;


    private final Function<@NotNull String, @Nullable String> function;


    /**
     * Constructs a dynamic placeholder.
     *
     * @param innerPattern the regex describing the token contents (without percent signs)
     * @param function     a function that receives the full matched token (including percent signs)
     *                     and returns the replacement text, or {@code null} to replace with empty
     */
    public DynamicPlaceholder(@NotNull final Pattern innerPattern,
                              @NotNull final Function<@NotNull String, @Nullable String> function) {

        this.pattern = Pattern.compile("%("+innerPattern.pattern() + ")%");
        this.function = function;

    }

    @Override
    @Nullable
    public String getValue(@NotNull final String replacing,
                           @NotNull final PlaceholderContext context) {
        return function.apply(replacing);
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

        if (!(o instanceof DynamicPlaceholder)) {
            return false;
        }
        DynamicPlaceholder that = (DynamicPlaceholder) o;
        return Objects.equals(this.getPattern(), that.getPattern());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getPattern());
    }
}
