package me.phoenixra.atumconfig.core.config;

import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.api.config.ConfigType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class AtumConfigSection extends AtumConfig {
    public AtumConfigSection(@NotNull ConfigManager configOwner, @NotNull ConfigType type, @Nullable Map<String, Object> values) {
        super(configOwner,type);
        if(values!=null) applyData(values);
    }
}
