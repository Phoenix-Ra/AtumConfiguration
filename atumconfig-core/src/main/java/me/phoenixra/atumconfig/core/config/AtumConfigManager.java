package me.phoenixra.atumconfig.core.config;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumconfig.api.ConfigOwner;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumconfig.api.config.ConfigManager;
import me.phoenixra.atumconfig.api.config.ConfigType;
import me.phoenixra.atumconfig.api.config.LoadableConfig;
import me.phoenixra.atumconfig.api.config.category.ConfigCategory;
import me.phoenixra.atumconfig.core.config.typehandlers.ConfigTypeHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redempt.crunch.functional.EvaluationEnvironment;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AtumConfigManager implements ConfigManager {

    @Getter
    protected ConfigOwner configOwner;
    protected Map<String, LoadableConfig> configs = new ConcurrentHashMap<>();
    protected Map<String, ConfigCategory> configCategoryRegistry = new ConcurrentHashMap<>();


    @Getter @Setter
    private EvaluationEnvironment evaluationEnvironment = new EvaluationEnvironment();
    public AtumConfigManager(ConfigOwner configOwner) {
        this.configOwner = configOwner;
    }

    @Override
    public @NotNull Config createConfig(@Nullable Map<String, Object> values, @NotNull ConfigType type) {
        return new AtumConfig(configOwner,type,values);
    }

    @Override
    public @NotNull Config createConfigFromStream(@NotNull InputStream stream, @NotNull ConfigType type) {
        InputStreamReader reader = new InputStreamReader(stream);
        String s = ConfigTypeHandler.readToString(reader);
        return createConfig(
                ConfigTypeHandler.toMap(getConfigOwner(),type,s),
                type
        );
    }

    @Override
    public @NotNull Config createConfigFromString(@NotNull String input, @NotNull ConfigType type) {
        return createConfig(
                ConfigTypeHandler.toMap(getConfigOwner(),type,input),
                type
        );
    }

    @Override
    public @NotNull LoadableConfig createLoadableConfig(@NotNull String name, @NotNull String directory,
                                                        @NotNull ConfigType type, boolean forceLoadResource
    ) {
        return new LoadableAtumConfig(configOwner,type,directory,name,forceLoadResource);
    }

    @Override
    public void reloadAll() {
        List<String> removal = new ArrayList<>();
        for (Map.Entry<String, LoadableConfig> entry : configs.entrySet()) {
            if (!entry.getValue().getFile().exists() || !entry.getValue().getFile().isFile()) {
                removal.add(entry.getKey());
                continue;
            }
            try {
                entry.getValue().reload();
            }catch (Exception exception){

                getConfigOwner().logError(
                        "Caught an Exception while " +
                                "trying to reload the" +
                                " config with name:"+ entry.getKey(),
                        exception
                );
            }
        }
        removal.forEach(configs::remove);

        for (Map.Entry<String, ConfigCategory> entry : configCategoryRegistry.entrySet()) {
            entry.getValue().reload();
        }
    }

    @Override
    public void saveAll() {
        List<String> removal = new ArrayList<>();
        for (Map.Entry<String, LoadableConfig> entry : configs.entrySet()) {
            if (!entry.getValue().getFile().exists() || !entry.getValue().getFile().isFile()) {
                removal.add(entry.getKey());
                continue;
            }
            try {
                entry.getValue().save();
            }catch (Exception exception){
                getConfigOwner().logError(
                        "Caught an Exception while" +
                                " trying to save the config with name:"+ entry.getKey(),
                        exception
                );
            }
        }
        removal.forEach(configs::remove);
    }


    @Override
    public @Nullable LoadableConfig getConfig(@NotNull String name) {
        return configs.get(name);
    }

    @Override
    public ConfigManager addConfig(@NotNull LoadableConfig config) {
        configs.put(config.getName(), config);
        return this;
    }

    @Override
    public void reloadConfigCategory(@NotNull String id) {
        ConfigCategory config = configCategoryRegistry.get(id);
        if (config == null) {
            return;
        }
        try {
            config.reload();
        }catch (Exception exception){
            getConfigOwner().logWarning(
                    "Caught an Exception while trying to reload the config category with id:"+ config.getId()+
                            "\n "+ Arrays.toString(exception.getStackTrace())
            );
        }
    }

    @Override
    public @Nullable ConfigCategory getConfigCategory(@NotNull String id) {
        return configCategoryRegistry.get(id);
    }
    @Override
    public void addConfigCategory(@NotNull ConfigCategory configCategory) {
        configCategoryRegistry.put(configCategory.getId(), configCategory);
    }

}
