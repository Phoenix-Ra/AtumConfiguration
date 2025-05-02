package me.phoenixra.atumconfig.api.placeholders.context;



import me.phoenixra.atumconfig.api.utils.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;




/**
 * A class that contains the injectable placeholders.
 *
 */
public class PlaceholderContext {
    @NotNull
    private final PlaceholderList placeholderList;
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

        if (!(o instanceof PlaceholderContext)) {
            return false;
        }
        PlaceholderContext that = (PlaceholderContext)o;
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
