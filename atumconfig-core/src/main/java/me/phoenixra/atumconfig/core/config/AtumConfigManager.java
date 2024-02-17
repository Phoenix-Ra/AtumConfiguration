package me.phoenixra.atumconfig.core.config;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumconfig.api.ConfigOwner;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumconfig.api.config.ConfigManager;
import me.phoenixra.atumconfig.api.config.ConfigType;
import me.phoenixra.atumconfig.api.config.LoadableConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redempt.crunch.functional.EvaluationEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AtumConfigManager implements ConfigManager {

    @Getter
    private ConfigOwner configOwner;
    private Map<String, LoadableConfig> configs = new ConcurrentHashMap<>();

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
                configOwner.logWarning(
                    "Caught an Exception while trying to reload the config with name:"+ entry.getKey()
                );
                getConfigOwner().logError(Arrays.toString(exception.getStackTrace()));
            }
        }
        removal.forEach(configs::remove);
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
                configOwner.logWarning(
                    "Caught an Exception while trying to save the config with name:"+ entry.getKey()
                );
                getConfigOwner().logError(Arrays.toString(exception.getStackTrace()));
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
}
