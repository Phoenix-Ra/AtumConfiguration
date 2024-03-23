package me.phoenixra.atumconfig.api.placeholders;


import me.phoenixra.atumconfig.api.placeholders.context.PlaceholderContext;
import me.phoenixra.atumconfig.api.placeholders.types.injectable.StaticPlaceholder;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An interface that represents a list of injectable placeholders
 * that is used in {@link PlaceholderContext}.
 */
public interface InjectablePlaceholderList {

    /**
     * Empty injectableContext object.
     *
     */
    InjectablePlaceholderList EMPTY_INJECTABLE = new InjectablePlaceholderList() {
        @Override
        public void addInjectablePlaceholder(@NotNull Iterable<InjectablePlaceholder> placeholders, boolean deep) {
            // Do nothing.
        }

        @Override
        public void removeInjectablePlaceholder(@NotNull Iterable<InjectablePlaceholder> placeholders, boolean deep) {
            // Do nothing.
        }

        @Override
        public void clearInjectedPlaceholders( boolean deep) {
            // Do nothing.
        }

        @Override
        public @NotNull
        List<InjectablePlaceholder> getPlaceholderInjections() {
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
    default void injectPlaceholders(boolean deep, @NotNull StaticPlaceholder... placeholders) {
        this.addInjectablePlaceholder(Arrays.stream(placeholders).collect(Collectors.toSet()), deep);
    }

    /**
     * Inject arguments.
     *
     * @param deep use true if you want to inject
     *             placeholder for the parent and all its children (for example config and its subsections).
     * @param placeholders The placeholders.
     */
    default void injectPlaceholders(boolean deep, @NotNull InjectablePlaceholder... placeholders) {
        this.addInjectablePlaceholder(Arrays.stream(placeholders).collect(Collectors.toSet()),deep);
    }

    /**
     * Remove arguments.
     *
     * @param deep use true if you want to remove
     *             placeholders for the parent and all its children (for example config and its subsections).
     * @param placeholders The placeholders.
     */
    default void removeInjectablePlaceholder(boolean deep, @NotNull InjectablePlaceholder... placeholders) {
        this.removeInjectablePlaceholder(Arrays.stream(placeholders).collect(Collectors.toSet()),deep);
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
    void addInjectablePlaceholder(@NotNull Iterable<InjectablePlaceholder> placeholders, boolean deep);

    /**
     * Remove placeholders
     * @param placeholders the placeholders to remove
     * @param deep use true if you want to remove
     *             placeholder for the parent and all its children (for example config and its subsections).
     */
    void removeInjectablePlaceholder(@NotNull Iterable<InjectablePlaceholder> placeholders, boolean deep);

    /**
     * Clear injected placeholders.
     * @param deep use true if you want to clear
     *             placeholders for the parent and all its children (for example config and its subsections).
     */
    void clearInjectedPlaceholders(boolean deep);

    /**
     * Get injected placeholders.
     * <p>
     * This method should always return an immutable list.
     *
     * @return Injected placeholders.
     */
    @NotNull
    List<InjectablePlaceholder> getPlaceholderInjections();
}
