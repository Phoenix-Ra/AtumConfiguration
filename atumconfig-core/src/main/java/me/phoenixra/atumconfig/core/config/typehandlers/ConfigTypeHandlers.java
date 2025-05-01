package me.phoenixra.atumconfig.core.config.typehandlers;

import me.phoenixra.atumconfig.api.config.ConfigType;

import java.util.HashMap;

public class ConfigTypeHandlers {
    public static HashMap<ConfigType, ConfigTypeHandler> HANDLERS = new HashMap<>();
    static {
        HANDLERS.put(ConfigType.JSON, new TypeHandlerJson());
        HANDLERS.put(ConfigType.YAML, new TypeHandlerYaml());

    }
}
