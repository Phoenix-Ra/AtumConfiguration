package me.phoenixra.atumconfig.core;


import lombok.Getter;
import me.phoenixra.atumconfig.api.ConfigLogger;
import me.phoenixra.atumconfig.api.placeholders.Placeholder;
import me.phoenixra.atumconfig.api.placeholders.PlaceholderHandler;
import me.phoenixra.atumconfig.api.placeholders.context.PlaceholderContext;
import me.phoenixra.atumconfig.api.tuples.PairRecord;
import me.phoenixra.atumconfig.api.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class AtumPlaceholderHandler implements PlaceholderHandler {
    private static final ExecutorService EXECUTOR =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


    @Getter
    private final ConfigLogger logger;


    @Getter
    private final Set<Placeholder> globalPlaceholders = new CopyOnWriteArraySet<>();


    public AtumPlaceholderHandler(@NotNull ConfigLogger logger){
        this.logger = logger;
    }


    @NotNull
    @Override
    public String translatePlaceholders(@NotNull final String text) {
        return translatePlaceholders(text, PlaceholderContext.EMPTY);

    }

    @NotNull
    @Override
    public String translatePlaceholders(@NotNull final String text,
                                        @NotNull final PlaceholderContext context) {

        List<Future<PairRecord<String, String>>> futures = new ArrayList<>();

        for (String textToReplace : PlaceholderHandler.findPlaceholdersIn(text)) {
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
                for (Placeholder placeholder : globalPlaceholders) {
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
                if (result.first().isEmpty()) continue;
                translated = StringUtils.replaceFast(translated,
                        result.first(),
                        result.second()
                );
            } catch (InterruptedException | ExecutionException e) {
                getLogger().logError(
                        "Placeholders exception ", e
                );

            }
        }

        return translated;
    }



    @Override
    public void registerGlobalPlaceholder(@NotNull final Placeholder placeholder) {
        globalPlaceholders.add(placeholder);
    }

    @Override
    public void unregisterGlobalPlaceholder(@NotNull Placeholder placeholder) {
        globalPlaceholders.remove(placeholder);
    }

}
