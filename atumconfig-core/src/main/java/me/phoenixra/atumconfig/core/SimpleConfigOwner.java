package me.phoenixra.atumconfig.core;

import lombok.Getter;
import me.phoenixra.atumconfig.api.ConfigOwner;
import me.phoenixra.atumconfig.api.config.ConfigManager;
import me.phoenixra.atumconfig.core.config.AtumConfigManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.logging.Logger;

public abstract class SimpleConfigOwner implements ConfigOwner {
    @Getter
    private String name;
    @Getter
    private File dataFolder;
    @Getter
    private Logger logger;

    @Getter
    private ConfigManager configManager;

    public SimpleConfigOwner(@NotNull String name,
                             @NotNull File dataFolder,
                             @NotNull Logger logger){
        this.name = name;
        this.dataFolder = dataFolder;
        this.logger = logger;

        this.configManager = new AtumConfigManager(this);
    }
}
