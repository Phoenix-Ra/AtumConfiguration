package me.phoenixra.atumconfig.api.placeholders.context;


import me.phoenixra.atumconfig.api.placeholders.Placeholder;
import me.phoenixra.atumconfig.api.placeholders.types.StaticPlaceholder;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines a mutable list of {@link Placeholder placeholders} that can be
 * applied to text. Supports shallow vs. deep injection: when deep is true,
 * placeholders propagate into nested contexts (e.g., subsections), otherwise
 * they apply only at the current level.
 */
public interface PlaceholderList {

    /**
     * A no-op placeholder list with no placeholders. All mutation methods
     * do nothing and {@link #getPlaceholders()} returns an empty list.
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
     * Convenience method to add one or more static placeholders.
     *
     * @param deep         if true, placeholders propagate to nested contexts
     * @param placeholders the static placeholders to add
     */
    default void addPlaceholders(boolean deep, @NotNull StaticPlaceholder... placeholders) {
        addPlaceholder(Arrays.stream(placeholders).collect(Collectors.toSet()), deep);
    }

    /**
     * Convenience method to add one or more placeholders.
     *
     * @param deep         if true, placeholders propagate to nested contexts
     * @param placeholders the placeholders to add
     */
    default void addPlaceholders(boolean deep, @NotNull Placeholder... placeholders) {
        addPlaceholder(Arrays.stream(placeholders).collect(Collectors.toSet()), deep);
    }

    /**
     * Convenience method to remove one or more placeholders.
     *
     * @param deep         if true, removal propagates to nested contexts
     * @param placeholders the placeholders to remove
     */
    default void removePlaceholder(boolean deep, @NotNull Placeholder... placeholders) {
        removePlaceholder(Arrays.stream(placeholders).collect(Collectors.toSet()), deep);
    }

    /**
     * Adds placeholders to this list.
     *
     * @param placeholders an iterable of placeholders to add
     * @param deep         if true, placeholders also apply deeply to nested contexts
     */
    void addPlaceholder(@NotNull Iterable<Placeholder> placeholders, boolean deep);

    /**
     * Removes placeholders from this list.
     *
     * @param placeholders an iterable of placeholders to remove
     * @param deep         if true, removal also applies deeply to nested contexts
     */
    void removePlaceholder(@NotNull Iterable<Placeholder> placeholders, boolean deep);

    /**
     * Clears all placeholders from this list.
     *
     * @param deep if true, clear also applies deeply to nested contexts
     */
    void clearPlaceholders(boolean deep);

    /**
     * Returns all currently registered placeholders in this list.
     *
     * @return a non-null list of placeholders
     */
    @NotNull
    List<Placeholder> getPlaceholders();
}
