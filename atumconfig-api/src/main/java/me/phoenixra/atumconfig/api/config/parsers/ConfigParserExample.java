package me.phoenixra.atumconfig.api.config.parsers;

import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumconfig.api.config.ConfigParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Example for ConfigParser
 */
public class ConfigParserExample implements ConfigParser<ExampleParseObj> {
    @Override
    public @Nullable Config toConfig(Object value, Config emptyConfig) {
        if(value instanceof ExampleParseObj parse){
            emptyConfig.set("id", parse.id());
            emptyConfig.set("test", parse.test());
            emptyConfig.set("value", parse.value());
            return emptyConfig;
        }
        return null;
    }

    @Override
    public ExampleParseObj fromConfig(Config config) {
        String id = config.getStringOrNull("id");
        Boolean test = config.getBoolOrNull("test");
        Integer value = config.getIntOrNull("value");
        if(id==null || test == null || value == null){
            return null;
        }
        return new ExampleParseObj(
                id, test, value
        );
    }

    @Override
    public @NotNull Class<ExampleParseObj> getClassParsed() {
        return ExampleParseObj.class;
    }
}
