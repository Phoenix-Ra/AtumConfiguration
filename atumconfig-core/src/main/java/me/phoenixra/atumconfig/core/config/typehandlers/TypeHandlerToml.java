package me.phoenixra.atumconfig.core.config.typehandlers;

import me.phoenixra.atumconfig.api.config.ConfigType;
import org.tomlj.Toml;
import org.tomlj.internal.TomlParser;

import java.util.Map;

public class TypeHandlerToml extends ConfigTypeHandler{
    public TypeHandlerToml() {
        super(ConfigType.TOML);
    }

    @Override
    protected Map<String, Object> parseToMap(String input) {
        return Toml.parse(input).toMap();
    }

    //@TODO add implementation
    @Override
    public String toString(Map<String, Object> map) {
        return "In development";
    }

}
