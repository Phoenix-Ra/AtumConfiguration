package me.phoenixra.atumconfig.api.placeholders;

import me.phoenixra.atumconfig.api.ConfigLogger;
import me.phoenixra.atumconfig.api.placeholders.context.PlaceholderContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public interface PlaceholderHandler {
    Pattern PATTERN = Pattern.compile("%([^% ]+)%");

    PlaceholderHandler EMPTY = new PlaceholderHandler() {
        @Override
        public void registerGlobalPlaceholder(@NotNull Placeholder placeholder) {}
        @Override
        public @NotNull Set<Placeholder> getGlobalPlaceholders() {return Set.of();}
        @Override
        public @NotNull String translatePlaceholders(@NotNull String text) {return "";}
        @Override
        public @NotNull String translatePlaceholders(@NotNull String text, @NotNull PlaceholderContext context) {return "";}
        @Override
        public @NotNull List<String> findPlaceholdersIn(@NotNull String text) {return List.of();}
        @Override
        public @NotNull ConfigLogger getLogger() {return ConfigLogger.EMPTY;}
    };


    void registerGlobalPlaceholder(@NotNull final Placeholder placeholder);

    @NotNull
    Set<Placeholder> getGlobalPlaceholders();

    @NotNull
    String translatePlaceholders(@NotNull final String text);

    @NotNull
    String translatePlaceholders(@NotNull final String text,
                                 @NotNull final PlaceholderContext context);

    @NotNull
    List<String> findPlaceholdersIn(@NotNull final String text);


    /**
     * Returns the logger used
     *
     * @return non-null {@link ConfigLogger} instance
     */
    @NotNull
    ConfigLogger getLogger();



}
