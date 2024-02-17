package me.phoenixra.atumconfig.core;

import lombok.Getter;
import me.phoenixra.atumconfig.api.ConfigOwner;
import me.phoenixra.atumconfig.api.config.ConfigManager;
import me.phoenixra.atumconfig.core.config.AtumConfigManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;


public abstract class SimpleConfigOwner implements ConfigOwner {
    @Getter
    private String name;
    @Getter
    private File dataFolder;

    @Getter
    private ConfigManager configManager;

    public SimpleConfigOwner(@NotNull String name,
                             @NotNull File dataFolder){
        this.name = name;
        this.dataFolder = dataFolder;

        this.configManager = new AtumConfigManager(this);
    }

}
