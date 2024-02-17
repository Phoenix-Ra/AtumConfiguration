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
        public void addInjectablePlaceholder(@NotNull Iterable<InjectablePlaceholder> placeholders) {
            // Do nothing.
        }

        @Override
        public void removeInjectablePlaceholder(@NotNull Iterable<InjectablePlaceholder> placeholders) {
            // Do nothing.
        }

        @Override
        public void clearInjectedPlaceholders() {
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
     * @param placeholders The placeholders.
     */
    default void injectPlaceholders(@NotNull StaticPlaceholder... placeholders) {
        this.addInjectablePlaceholder(Arrays.stream(placeholders).collect(Collectors.toSet()));
    }

    /**
     * Inject arguments.
     *
     * @param placeholders The placeholders.
     */
    default void injectPlaceholders(@NotNull InjectablePlaceholder... placeholders) {
        this.addInjectablePlaceholder(Arrays.stream(placeholders).collect(Collectors.toSet()));
    }

    /**
     * Remove arguments.
     *
     * @param placeholders The placeholders.
     */
    default void removeInjectablePlaceholder(@NotNull InjectablePlaceholder... placeholders) {
        this.removeInjectablePlaceholder(Arrays.stream(placeholders).collect(Collectors.toSet()));
    }


    /**
     * Inject placeholders.
     * <p>
     * If a placeholder already has the same pattern, it should be replaced.
     *
     * @param placeholders The placeholders.
     */
    void addInjectablePlaceholder(@NotNull Iterable<InjectablePlaceholder> placeholders);

    /**
     * Remove placeholders
     */
    void removeInjectablePlaceholder(@NotNull Iterable<InjectablePlaceholder> placeholders);

    /**
     * Clear injected placeholders.
     */
    void clearInjectedPlaceholders();

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
