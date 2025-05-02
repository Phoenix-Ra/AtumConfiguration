package me.phoenixra.atumconfig.api.config;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Represents supported configuration file formats,
 * each associated with a specific file extension.
 */
public enum ConfigType {

    /**
     * JSON format (files ending with <code>.json</code>).
     */
    JSON("json"),

    /**
     * YAML format (files ending with <code>.yml</code>).
     */
    YAML("yml");


    /**
     * The file extension (without the leading dot) for this config type.
     */
    @Getter
    private final String fileExtension;


    ConfigType(@NotNull final String fileExtension) {
        this.fileExtension = fileExtension;
    }

    /**
     * Determines the {@link ConfigType} based on the extension of the given file's name.
     * <p>
     * Matches the text after the last dot. If it equals <code>"yml"</code>, returns {@link #YAML}.
     * If it equals <code>"json"</code>, returns {@link #JSON}. Otherwise, returns <code>null</code>
     *
     * @param file the file whose extension will be inspected
     * @return the matching ConfigType, or <code>null</code> if no known extension is found
     */
    @Nullable
    public static ConfigType fromFile(File file) {
        String name = file.getName();
        String[] parts = name.split("\\.");
        String ext = parts.length > 1 ? parts[parts.length - 1] : "";
        switch (ext) {
            case "yml":
                return YAML;
            case "json":
                return JSON;
            default:
                return null;
        }
    }

}
