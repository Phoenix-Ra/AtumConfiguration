package me.phoenixra.atumconfig.core;


import lombok.Getter;
import me.phoenixra.atumconfig.api.ConfigLogger;
import me.phoenixra.atumconfig.api.placeholders.Placeholder;
import me.phoenixra.atumconfig.api.placeholders.PlaceholderHandler;
import me.phoenixra.atumconfig.api.placeholders.context.PlaceholderContext;
import me.phoenixra.atumconfig.api.tuples.PairRecord;
import me.phoenixra.atumconfig.api.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AtumPlaceholderHandler implements PlaceholderHandler {

    @Getter
    private final ConfigLogger logger;


    private final Set<Placeholder> placeholders = new CopyOnWriteArraySet<>();

    private static final ExecutorService EXECUTOR =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public AtumPlaceholderHandler(@NotNull ConfigLogger logger){
        this.logger = logger;
    }

    /**
     * Register an arguments.
     *
     * @param placeholder The arguments to register.
     */
    @Override
    public void registerGlobalPlaceholder(@NotNull final Placeholder placeholder) {
        placeholders.add(placeholder);
    }

    /**
     * Translate all placeholders without a placeholder context.
     *
     * @param text The text that may contain placeholders to translate.
     * @return The text, translated.
     */
    @NotNull
    @Override
    public String translatePlaceholders(@NotNull final String text) {
        return translatePlaceholders(text, PlaceholderContext.EMPTY);

    }

    /**
     * Translate all placeholders in a translation context.
     *
     * @param text        The text that may contain placeholders to translate.
     * @param context     The translation context.
     * @return The text, translated.
     */
    @NotNull
    @Override
    public String translatePlaceholders(@NotNull final String text,
                                        @NotNull final PlaceholderContext context) {

        List<Future<PairRecord<String, String>>> futures = new ArrayList<>();

        for (String textToReplace : findPlaceholdersIn(text)) {
            Future<PairRecord<String, String>> future = EXECUTOR.submit(() -> {
                for (Placeholder placeholder : context.placeholderList().getPlaceholders()) {
                    if (textToReplace.matches(placeholder.getPattern().pattern())) {
                        String replacement = placeholder.getValue(textToReplace, context);
                        if (replacement == null) return new PairRecord<>("", "");
                        return new PairRecord<>(
                                textToReplace,
                                replacement
                        );
                    }
                }
                for (Placeholder placeholder : placeholders) {
                    if (textToReplace.matches(placeholder.getPattern().pattern())) {
                        String replacement = placeholder.getValue(textToReplace, context);
                        if (replacement == null) return new PairRecord<>("", "");
                        return new PairRecord<>(
                                textToReplace,
                                replacement
                        );
                    }
                }
                return new PairRecord<>("", "");
            });
            futures.add(future);
        }

        String translated = text;
        for (Future<PairRecord<String, String>> future : futures) {
            try {
                PairRecord<String, String> result = future.get();
                if (result.getFirst().isEmpty()) continue;
                translated = StringUtils.replaceFast(translated,
                        result.getFirst(),
                        result.getSecond()
                );
            } catch (InterruptedException | ExecutionException e) {
                getLogger().logError(
                        "Placeholders exception ", e
                );

            }
        }

        return translated;
    }

    /**
     * Find all placeholders in a given text.
     *
     * @param text The text.
     * @return The placeholders.
     */
    @Override
    public @NotNull List<String> findPlaceholdersIn(@NotNull final String text) {
        Set<String> found = new HashSet<>();

        Matcher matcher = PATTERN.matcher(text);
        while (matcher.find()) {
            found.add(matcher.group());
        }

        return new ArrayList<>(found);
    }

    /**
     * Get all registered placeholders for a config owner.
     *
     * @return The placeholders.
     */
    @Override
    public @NotNull Set<Placeholder> getGlobalPlaceholders() {
        return placeholders;
    }

}
