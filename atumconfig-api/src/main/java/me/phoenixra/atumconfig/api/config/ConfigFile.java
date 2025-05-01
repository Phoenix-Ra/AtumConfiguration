package me.phoenixra.atumconfig.api.config;


import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;


public interface ConfigFile extends Config {

    /**
     * Create the file.
     *
     * @param forceResourceLoad if true - throws NullPointerException
     *                          when file not found inside the resources folder
     */
    void createFile(boolean forceResourceLoad);

    /**
     * Reload the config.
     *
     * @throws IOException If error has occurred while reloading
     */
    void reload() throws IOException;

    /**
     * Save the config.
     *
     * @throws IOException If error has occurred while saving
     */
    void save() throws IOException;

    /**
     * Relative to root file path
     * @return
     */
    @NotNull
    Path getRelativeFilePath();
    /**
     * Get the config file.
     *
     * @return The file.
     */
    @NotNull
    File getFile();

    /**
     * Get the full file name
     *
     * @return The file name.
     */
    @NotNull
    default String getFileName(){
        return getFile().getName();
    }

    /**
     * Get the name of a config
     *
     * @return The name.
     */
    default @NotNull String getName(){
        return getFileName().replace("."+getType().getFileExtension(),"");
    }

    /**
     * Get the path to a file inside the .jar
     *
     * @return The file.
     */
    @NotNull
    String getResourcePath();


    @NotNull String getId();
}
