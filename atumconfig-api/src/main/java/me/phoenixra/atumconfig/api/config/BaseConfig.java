package me.phoenixra.atumconfig.api.config;


import me.phoenixra.atumconfig.api.ConfigOwner;
import me.phoenixra.atumconfig.api.config.wrapper.LoadableConfigWrapper;
import org.jetbrains.annotations.NotNull;

public class BaseConfig extends LoadableConfigWrapper {
    protected BaseConfig(
                @NotNull ConfigOwner configOwner,
                @NotNull String configName,
                @NotNull ConfigType configType,
                boolean forceLoadResource) {

        super(configOwner.getConfigManager().createLoadableConfig(
            configName,
            "",
            configType,
            forceLoadResource,
                false
                )
        );
    }
}
