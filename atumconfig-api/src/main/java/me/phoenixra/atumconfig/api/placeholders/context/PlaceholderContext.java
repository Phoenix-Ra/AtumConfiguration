package me.phoenixra.atumconfig.api.placeholders.context;



import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;


/**
 * A class that contains the injectable placeholders.
 *
 * @param placeholderList The PlaceholderInjectable context.
 */
public record PlaceholderContext(@NotNull PlaceholderList placeholderList) {

    /**
     * Empty Context, containing empty placeholder list
     */
    public static final PlaceholderContext EMPTY = new PlaceholderContext(
            PlaceholderList.EMPTY
    );

    /**
     * Constructs a new PlaceholderContext with the given parameters.
     *
     * @param placeholderList The PlaceholderInjectable parseContext.
     */
    public PlaceholderContext(@Nullable final PlaceholderList placeholderList) {
        this.placeholderList = Objects.requireNonNullElse(placeholderList, PlaceholderList.EMPTY);
    }

    /**
     * Get the PlaceholderInjectable context.
     *
     * @return The PlaceholderInjectable context.
     */
    @Override
    @NotNull
    public PlaceholderList placeholderList() {
        return placeholderList;
    }

    /**
     * Copy with an extra injectable context.
     *
     * @param injectableContext The injectable context to add.
     * @return The new context.
     */
    public PlaceholderContext withContext(@NotNull final PlaceholderList injectableContext) {
        return new PlaceholderContext(
                new PlaceholderListMerged(this.placeholderList(), injectableContext)
        );
    }

    /**
     * Create PlaceholderContext of a PlaceholderInjectable parseContext.
     *
     * @param injectableContext The PlaceholderInjectable parseContext.
     * @return The context.
     */
    public static PlaceholderContext of(@NotNull final PlaceholderList injectableContext) {
        return new PlaceholderContext(
                injectableContext
        );
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof PlaceholderContext that)) {
            return false;
        }
        return placeholderList().equals(that.placeholderList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(placeholderList());
    }

    @Override
    public String toString() {
        return "PlaceholderContext{" +
                ", injectableContext=" + placeholderList +
                '}';
    }


}
