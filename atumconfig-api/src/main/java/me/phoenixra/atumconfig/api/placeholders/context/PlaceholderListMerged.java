package me.phoenixra.atumconfig.api.placeholders.context;

import me.phoenixra.atumconfig.api.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Combines two {@link PlaceholderList} instances into a single merged context,
 * preserving the base and additional placeholders while allowing extra, ad-hoc
 * injections that can be added or removed independently.
 * <p>
 * Placeholders added or removed via this merged list are propagated to both
 * the base and additional contexts when the {@code deep} flag is true.
 * Otherwise, they are only recorded locally in this merged instance.
 */
public class PlaceholderListMerged implements PlaceholderList {

    private final PlaceholderList baseContext;


    private final PlaceholderList additionalContext;

    private final Set<Placeholder> extraInjections = new HashSet<>();

    /**
     * Constructs a merged placeholder list that overlays two existing contexts.
     *
     * @param baseContext        the base placeholder list (non-null)
     * @param additionalContext  the additional placeholder list (non-null)
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
        extraInjections.clear();
        if(deep) {
            baseContext.clearPlaceholders(deep);
            additionalContext.clearPlaceholders(deep);
        }
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

        if (!(o instanceof PlaceholderListMerged)) {
            return false;
        }
        PlaceholderListMerged that = (PlaceholderListMerged)o;
        return Objects.equals(baseContext, that.baseContext)
                && Objects.equals(additionalContext, that.additionalContext)
                && Objects.equals(extraInjections, that.extraInjections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseContext, additionalContext, extraInjections);
    }
}
