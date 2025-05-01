package me.phoenixra.atumconfig.api.placeholders.context;


import me.phoenixra.atumconfig.api.placeholders.Placeholder;
import me.phoenixra.atumconfig.api.placeholders.types.StaticPlaceholder;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An interface that represents a list of injectable placeholders
 * that is used in {@link PlaceholderContext}.
 */
public interface PlaceholderList {

    /**
     * Empty injectableContext object.
     *
     */
    PlaceholderList EMPTY = new PlaceholderList() {
        @Override
        public void addPlaceholder(@NotNull Iterable<Placeholder> placeholders, boolean deep) {
            // Do nothing.
        }

        @Override
        public void removePlaceholder(@NotNull Iterable<Placeholder> placeholders, boolean deep) {
            // Do nothing.
        }

        @Override
        public void clearPlaceholders(boolean deep) {
            // Do nothing.
        }

        @Override
        public @NotNull
        List<Placeholder> getPlaceholders() {
            return Collections.emptyList();
        }
    };


    /**
     * Inject arguments.
     *
     * @param deep use true if you want to inject
     *             placeholder for the parent and all its children (for example config and its subsections).
     * @param placeholders The placeholders.
     */
    default void addPlaceholders(boolean deep, @NotNull StaticPlaceholder... placeholders) {
        this.addPlaceholder(Arrays.stream(placeholders).collect(Collectors.toSet()), deep);
    }

    /**
     * Inject arguments.
     *
     * @param deep use true if you want to inject
     *             placeholder for the parent and all its children (for example config and its subsections).
     * @param placeholders The placeholders.
     */
    default void addPlaceholders(boolean deep, @NotNull Placeholder... placeholders) {
        this.addPlaceholder(Arrays.stream(placeholders).collect(Collectors.toSet()),deep);
    }

    /**
     * Remove arguments.
     *
     * @param deep use true if you want to remove
     *             placeholders for the parent and all its children (for example config and its subsections).
     * @param placeholders The placeholders.
     */
    default void removePlaceholder(boolean deep, @NotNull Placeholder... placeholders) {
        this.removePlaceholder(Arrays.stream(placeholders).collect(Collectors.toSet()),deep);
    }


    /**
     * Inject placeholders.
     * <p>
     * If a placeholder already has the same pattern, it should be replaced.
     *
     * @param placeholders The placeholders.
     * @param deep use true if you want to inject
     *             placeholder for the parent and all its children (for example config and its subsections).
     */
    void addPlaceholder(@NotNull Iterable<Placeholder> placeholders, boolean deep);

    /**
     * Remove placeholders
     * @param placeholders the placeholders to remove
     * @param deep use true if you want to remove
     *             placeholder for the parent and all its children (for example config and its subsections).
     */
    void removePlaceholder(@NotNull Iterable<Placeholder> placeholders, boolean deep);

    /**
     * Clear injected placeholders.
     * @param deep use true if you want to clear
     *             placeholders for the parent and all its children (for example config and its subsections).
     */
    void clearPlaceholders(boolean deep);

    /**
     * Get injected placeholders.
     * <p>
     * This method should always return an immutable list.
     *
     * @return Injected placeholders.
     */
    @NotNull
    List<Placeholder> getPlaceholders();
}
