package me.phoenixra.atumconfig.api.placeholders.types;

import me.phoenixra.atumconfig.api.placeholders.Placeholder;
import me.phoenixra.atumconfig.api.placeholders.context.PlaceholderContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
public class DynamicPlaceholder implements Placeholder {
    /**
     * The arguments pattern.
     */
    private final Pattern pattern;

    /**
     * The function to retrieve the output of the arguments.
     */
    private final Function<@NotNull String, @Nullable String> function;


    /**
     * Create a new dynamic arguments.
     *
     * @param innerPattern     The pattern.
     * @param function         The function to retrieve the value.
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
