package me.phoenixra.atumconfig.api.config.wrapper;


import me.phoenixra.atumconfig.api.config.LoadableConfig;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class LoadableConfigWrapper extends ConfigWrapper<LoadableConfig> implements LoadableConfig {
    protected LoadableConfigWrapper(@NotNull final LoadableConfig handle) {
        super(handle);
    }
    @Override
    public void createFile(boolean forceResourceLoad) {
        getHandle().createFile(forceResourceLoad);
    }

    @Override
    public void reload() throws IOException {
        getHandle().reload();
    }

    @Override
    public void save() throws IOException {
        getHandle().save();
    }

    @Override
    public File getFile() {
        return getHandle().getFile();
    }

    @Override
    public String getResourcePath() {
        return getHandle().getResourcePath();
    }

}
