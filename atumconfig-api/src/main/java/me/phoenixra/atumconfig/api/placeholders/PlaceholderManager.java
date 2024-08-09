package me.phoenixra.atumconfig.api.placeholders;


import me.phoenixra.atumconfig.api.ConfigOwner;
import me.phoenixra.atumconfig.api.placeholders.context.PlaceholderContext;
import me.phoenixra.atumconfig.api.tuples.PairRecord;
import me.phoenixra.atumconfig.api.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderManager {

    /**
     * All registered placeholders.
     */
    private static final HashMap<ConfigOwner,
            Set<RegistrablePlaceholder>> REGISTERED_PLACEHOLDERS = new HashMap<>();

    /**
     * The default PlaceholderAPI pattern; brought in for compatibility.
     */
    private static final Pattern PATTERN = Pattern.compile("%([^% ]+)%");


    /**
     * Register an arguments.
     *
     * @param placeholder The arguments to register.
     */
    public static void registerPlaceholder(@NotNull final RegistrablePlaceholder placeholder) {
        if (!REGISTERED_PLACEHOLDERS.containsKey(placeholder.getConfigOwner())) {
            REGISTERED_PLACEHOLDERS.put(placeholder.getConfigOwner(), new HashSet<>());
        }
        REGISTERED_PLACEHOLDERS.get(placeholder.getConfigOwner()).add(placeholder);
    }

    /**
     * Translate all placeholders without a placeholder context.
     *
     * @param configOwner the config owner
     * @param text The text that may contain placeholders to translate.
     * @return The text, translated.
     */
    @NotNull
    public static String translatePlaceholders(@NotNull ConfigOwner configOwner, @NotNull final String text) {
        return translatePlaceholders(configOwner, text, PlaceholderContext.EMPTY);

    }

    /**
     * Translate all placeholders in a translation context.
     *
     * @param configOwner The config owner that is translating the text.
     * @param text        The text that may contain placeholders to translate.
     * @param context     The translation context.
     * @return The text, translated.
     */
    @NotNull
    public static String translatePlaceholders(@NotNull ConfigOwner configOwner,
                                               @NotNull final String text,
                                               @NotNull final PlaceholderContext context
    ) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        List<Future<PairRecord<String, String>>> futures = new ArrayList<>();

        for (String textToReplace : findPlaceholdersIn(text)) {
            Future<PairRecord<String, String>> future = executor.submit(() -> {
                for (InjectablePlaceholder placeholder : context.getInjectableContext().getPlaceholderInjections()) {
                    if (textToReplace.matches(placeholder.getPattern().pattern())) {
                        String replacement = placeholder.getValue(textToReplace, context);
                        if (replacement == null) return new PairRecord<>("", "");
                        return new PairRecord<>(
                                textToReplace,
                                replacement
                        );
                    }
                }
                for (RegistrablePlaceholder placeholder : REGISTERED_PLACEHOLDERS.getOrDefault(configOwner, new HashSet<>())) {
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
                PairRecord<String, String> out = future.get();
                if (out.getFirst().isEmpty()) continue;
                translated = StringUtils.replaceFast(translated,
                        out.getFirst(),
                        out.getSecond()
                );
            } catch (InterruptedException | ExecutionException e) {
                configOwner.logError(
                        null, e
                );

            }
        }

        executor.shutdown();

        return translated;
    }

    /**
     * Find all placeholders in a given text.
     *
     * @param text The text.
     * @return The placeholders.
     */
    public static List<String> findPlaceholdersIn(@NotNull final String text) {
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
     * @param configOwner The config owner.
     * @return The placeholders.
     */
    public static Set<RegistrablePlaceholder> getRegisteredPlaceholders(@NotNull final ConfigOwner configOwner) {
        return REGISTERED_PLACEHOLDERS.get(configOwner);
    }

    private PlaceholderManager() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
