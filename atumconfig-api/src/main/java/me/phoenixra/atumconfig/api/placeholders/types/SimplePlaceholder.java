package me.phoenixra.atumconfig.api.placeholders.types;

import me.phoenixra.atumconfig.api.ConfigOwner;
import me.phoenixra.atumconfig.api.placeholders.RegistrablePlaceholder;
import me.phoenixra.atumconfig.api.placeholders.context.PlaceholderContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;
public class SimplePlaceholder implements RegistrablePlaceholder {
    /**
     * The arguments pattern.
     */
    private final Pattern pattern;

    /**
     * The function to retrieve the output of the arguments.
     */
    private final Supplier<@Nullable String> function;

    /**
     * The config owner for the arguments.
     */
    private final ConfigOwner configOwner;

    /**
     * Create a new player arguments.
     *
     * @param configOwner     The configOwner.
     * @param identifier The identifier.
     * @param function   The function to retrieve the value.
     */
    public SimplePlaceholder(@NotNull final ConfigOwner configOwner,
                             @NotNull final String identifier,
                             @NotNull final Supplier<@Nullable String> function) {
        this.configOwner = configOwner;
        this.pattern = Pattern.compile("%"+identifier+"%");
        this.function = function;
    }

    @Override
    public @Nullable String getValue(@NotNull final String args,
                                     @NotNull final PlaceholderContext context) {
        return function.get();
    }

    @Override
    public @NotNull ConfigOwner getConfigOwner() {
        return this.configOwner;
    }

    @NotNull
    @Override
    public Pattern getPattern() {
        return this.pattern;
    }

    @Override
    public @NotNull SimplePlaceholder register() {
        return (SimplePlaceholder) RegistrablePlaceholder.super.register();
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SimplePlaceholder)) {
            return false;
        }
        SimplePlaceholder that = (SimplePlaceholder) o;
        return Objects.equals(this.getPattern(), that.getPattern())
                && Objects.equals(this.getConfigOwner(), that.getConfigOwner());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getPattern(), this.getConfigOwner());
    }
}
