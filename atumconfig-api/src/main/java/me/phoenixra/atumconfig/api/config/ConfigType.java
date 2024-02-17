package me.phoenixra.atumconfig.api.config;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public enum ConfigType {
    /**
     * .json file
     */
    JSON("json"),

    /**
     * .yml file
     */
    YAML("yml"),

    /**
     * .toml file
     * <p>NOT IMPLEMENTED YET</p>
     */
    TOML("toml");


    @Getter
    private final String fileExtension;

    ConfigType(@NotNull final String fileExtension) {
        this.fileExtension = fileExtension;
    }

    /**
     *
     *
     * @param file the file
     * @return the config type
     */
    @NotNull
    public static ConfigType fromFile(File file){
        switch (file.getName().split("\\.")[0]){
            case "yml":
                return ConfigType.YAML;
            case "json":
                return ConfigType.JSON;
            case "toml":
                return ConfigType.TOML;
        }
        return ConfigType.YAML;
    }

}
