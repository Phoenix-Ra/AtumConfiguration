package me.phoenixra.atumconfig.api;

import me.phoenixra.atumconfig.api.config.ConfigManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.logging.Logger;

public interface ConfigOwner {

    @NotNull
    ConfigManager getConfigManager();


    @NotNull
    Logger getLogger();

    @NotNull
    File getDataFolder();

    @NotNull
    String getName();

    boolean supportMinecraft();

}
