package me.phoenixra.atumconfig.api.placeholders.context;

import me.phoenixra.atumconfig.api.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlaceholderListMerged implements PlaceholderList {
    /**
     * The base context.
     */
    private final PlaceholderList baseContext;

    /**
     * The additional context.
     */
    private final PlaceholderList additionalContext;

    /**
     * Extra injections.
     */
    private final Set<Placeholder> extraInjections = new HashSet<>();

    /**
     * Create a new merged injectable context.
     *
     * @param baseContext       The base context.
     * @param additionalContext The additional context.
     */
    public PlaceholderListMerged(@NotNull final PlaceholderList baseContext,
                                 @NotNull final PlaceholderList additionalContext) {
        this.baseContext = baseContext;
        this.additionalContext = additionalContext;
    }

    @Override
    public void addPlaceholder(@NotNull final Iterable<Placeholder> placeholders, boolean deep) {
        for (Placeholder placeholder : placeholders) {
            extraInjections.add(placeholder);
        }
        if(deep){
            baseContext.addPlaceholder(placeholders,deep);
            additionalContext.addPlaceholder(placeholders,deep);
        }
    }

    @Override
    public void removePlaceholder(@NotNull Iterable<Placeholder> placeholders, boolean deep) {
        for (Placeholder placeholder : placeholders) {
            extraInjections.remove(placeholder);
        }
        if(deep){
            baseContext.removePlaceholder(placeholders,deep);
            additionalContext.removePlaceholder(placeholders,deep);
        }
    }

    @Override
    public void clearPlaceholders(boolean deep) {
        baseContext.clearPlaceholders(deep);
        additionalContext.clearPlaceholders(deep);
        extraInjections.clear();
    }

    @Override
    public @NotNull List<Placeholder> getPlaceholders() {
        List<Placeholder> base = baseContext.getPlaceholders();
        List<Placeholder> additional = additionalContext.getPlaceholders();

        List<Placeholder> injections = new ArrayList<>(base.size() + additional.size() + extraInjections.size());

        injections.addAll(base);
        injections.addAll(additional);
        injections.addAll(extraInjections);

        return injections;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof PlaceholderListMerged that)) {
            return false;
        }
        return Objects.equals(baseContext, that.baseContext)
                && Objects.equals(additionalContext, that.additionalContext)
                && Objects.equals(extraInjections, that.extraInjections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseContext, additionalContext, extraInjections);
    }
}
