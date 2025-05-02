package me.phoenixra.atumconfig.core;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumconfig.api.ConfigLogger;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.api.config.ConfigParser;
import me.phoenixra.atumconfig.api.config.ConfigType;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.atumconfig.api.config.catalog.ConfigCatalog;
import me.phoenixra.atumconfig.api.config.catalog.ConfigCatalogListener;
import me.phoenixra.atumconfig.api.placeholders.PlaceholderHandler;
import me.phoenixra.atumconfig.core.config.AtumConfig;
import me.phoenixra.atumconfig.core.config.AtumConfigFile;
import me.phoenixra.atumconfig.core.config.AtumConfigCatalog;
import me.phoenixra.atumconfig.core.config.typehandlers.ConfigTypeHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redempt.crunch.functional.EvaluationEnvironment;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class AtumConfigManager implements ConfigManager {

    @Getter
    private final String id;
    @Getter
    private final Path directory;
    @Getter
    private final ConfigLogger logger;
    private final boolean supportColorCodes;

    @Setter
    private PlaceholderHandler placeholderHandler;

    @Getter
    protected Map<String, ConfigFile> configFilesMap = new ConcurrentHashMap<>();
    @Getter
    protected Map<String, ConfigCatalog> configCatalogsMap = new ConcurrentHashMap<>();

    @Getter
    protected Map<Class<?>, ConfigParser<?>> configParsersMap = new ConcurrentHashMap<>();



    @Getter @Setter
    private EvaluationEnvironment evaluationEnvironment = new EvaluationEnvironment();
    public AtumConfigManager(@NotNull String id,
                             @NotNull Path directory,
                             @NotNull ConfigLogger logger,
                             boolean supportColorCodes) {
        this.id = id;
        this.directory = directory;
        this.logger = logger;
        this.supportColorCodes = supportColorCodes;
    }
    public AtumConfigManager(@NotNull String id,
                             @NotNull Path directory,
                             boolean supportColorCodes) {
        this.id = id;
        this.directory = directory;
        this.logger = ConfigLogger.SIMPLE;
        this.supportColorCodes = supportColorCodes;
    }

    @Override
    public boolean supportsColorCodes() {
        return supportColorCodes;
    }

    @Override
    public @NotNull Config createConfig(@NotNull ConfigType type,
                                        @Nullable Map<String, Object> valuesMap) {
        return new AtumConfig(this, type, valuesMap);
    }

    @Override
    public @NotNull Config createConfigFromStream(@NotNull ConfigType type,
                                                  @NotNull InputStream stream) {
        InputStreamReader reader = new InputStreamReader(stream);
        String s = ConfigTypeHandler.readToString(reader);
        return createConfig(
                type,
                ConfigTypeHandler.toMap(this,type,s)
        );
    }

    @Override
    public @NotNull Config createConfigFromString(@NotNull ConfigType type,
                                                  @NotNull String input) {
        return createConfig(
                type,
                ConfigTypeHandler.toMap(this,type,input)
        );
    }

    @Override
    public @NotNull ConfigFile createConfigFile(@NotNull ConfigType type,
                                                @NotNull String id,
                                                @NotNull Path relativePath,
                                                boolean forceLoadResource
    ) throws IOException {
        AtumConfigFile config = new AtumConfigFile(this, type, id, relativePath, forceLoadResource);;
        this.addConfigFile(config);
        return config;
    }

    @Override
    public @NotNull ConfigCatalog createCatalog(@NotNull ConfigType type,
                                                @NotNull String id,
                                                @NotNull Path relativeDirectory,
                                                boolean nested,
                                                @NotNull ConfigCatalogListener catalogListener) {
        AtumConfigCatalog catalog = new AtumConfigCatalog(this, type, id, relativeDirectory, nested, catalogListener);;
        this.addCatalog(catalog);
        return catalog;
    }

    @Override
    public void reloadAll() {
        List<String> removal = new ArrayList<>();
        for (Map.Entry<String, ConfigFile> entry : configFilesMap.entrySet()) {
            if (!entry.getValue().getFile().exists() || !entry.getValue().getFile().isFile()) {
                removal.add(entry.getKey());
                continue;
            }
            try {
                entry.getValue().reload();
            }catch (Exception exception){

                getLogger().logError(
                        "Caught an Exception while " +
                                "trying to reload the" +
                                " config with name:"+ entry.getKey(),
                        exception
                );
            }
        }
        removal.forEach(configFilesMap::remove);

        configCatalogsMap.values().forEach(ConfigCatalog::reload);

    }




    @Override
    public @NotNull ConfigManager addConfigFile(@NotNull ConfigFile config) {
        configFilesMap.put(config.getName(), config);
        return this;
    }

    @Override
    public @NotNull ConfigManager addCatalog(@NotNull ConfigCatalog configCategory) {
        configCatalogsMap.put(configCategory.getId(), configCategory);
        return this;
    }


    @Override
    public @NotNull Optional<ConfigFile> getConfigFile(@NotNull String name) {
        return Optional.ofNullable(configFilesMap.get(name));
    }

    @Override
    public @NotNull Optional<ConfigCatalog> getCatalog(@NotNull String id) {
        return Optional.ofNullable(configCatalogsMap.get(id));
    }

    @Override
    public @NotNull Optional<PlaceholderHandler> getPlaceholderHandler() {
        return Optional.ofNullable(placeholderHandler);
    }

    @Override
    public <T> @NotNull Optional<ConfigParser<T>> getConfigParser(@NotNull Class<T> clazz) {
        return Optional.ofNullable((ConfigParser<T>)configParsersMap.get(clazz));
    }

    @Override
    public void addConfigParser(@NotNull ConfigParser<?> configParser) {
        configParsersMap.put(configParser.getClassParsed(), configParser);
    }
}
