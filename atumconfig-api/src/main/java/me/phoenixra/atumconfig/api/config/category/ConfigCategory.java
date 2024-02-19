package me.phoenixra.atumconfig.api.config.category;

import lombok.Getter;
import me.phoenixra.atumconfig.api.ConfigOwner;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumconfig.api.config.ConfigType;
import me.phoenixra.atumconfig.api.utils.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import me.phoenixra.atumconfig.api.tuples.PairRecord;
public abstract class ConfigCategory {
    @Getter
    private final ConfigOwner configOwner;
    private final ConfigType configType;
    private final String id;
    private final String directory;
    private final boolean supportSubFolders;

    /**
     * Config Category class
     *
     * @param configOwner       the config owner
     * @param configType        the config type
     * @param id                the category id
     * @param directory         the directory path
     * @param supportSubFolders if accept configs from subFolders
     */
    public ConfigCategory(@NotNull ConfigOwner configOwner,
                          @NotNull ConfigType configType,
                          @NotNull String id,
                          @NotNull String directory,
                          boolean supportSubFolders) {
        this.configOwner = configOwner;
        this.configType = configType;
        this.id = id;
        this.directory = directory;
        this.supportSubFolders = supportSubFolders;
    }

    /**
     * Reload the config category
     */
    public final void reload() {
        beforeReload();
        clear();
        File dir = new File(configOwner.getDataFolder(), directory);
        getConfigOwner().logInfo("Reloading " + id + " configs...");
        //@TODO add check for missing files
        if (!dir.exists()) {
            loadDefaults();
        }
        for (PairRecord<String, File> entry : FileUtils.loadFiles(dir, supportSubFolders)) {
            Config conf = getConfigOwner().getConfigManager().createLoadableConfig(
                    entry.getSecond().getName().split("\\.")[0],
                    directory,
                    configType,
                    false
            );
            acceptConfig(entry.getFirst(), conf);
        }
        afterReload();
    }

    private void loadDefaults() {
        for (String path : FileUtils.getAllPathsInResourceFolder(configOwner, directory)) {
            try {
                File file = new File(configOwner.getDataFolder(), path);
                if (!file.getName().contains(".")) {
                    file.mkdir();
                    continue;
                }
                InputStream stream = configOwner.getClass().getResourceAsStream(path);
                getConfigOwner().logInfo("Loading default config " + path + " CLASS: " + configOwner.getClass());
                if (stream == null) continue;
                Files.copy(stream, Paths.get(file.toURI()), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                getConfigOwner().logError(Arrays.toString(e.getStackTrace()));
            }
        }

    }

    /**
     * Clear the saved data
     */
    protected abstract void clear();

    /**
     * Accept the config object loaded from file
     *
     * @param id the config id
     * @param config the config object
     */
    protected abstract void acceptConfig(@NotNull String id, @NotNull Config config);

    /**
     * Called before category reload
     * <br><br>
     * Override to add implementation
     */
    public void beforeReload() {
    }

    /**
     * Called after category reload
     * <br><br>
     * Override to add implementation
     */
    public void afterReload() {
    }


    /**
     * Get the ID of a category
     *
     * @return The id
     */
    public final @NotNull String getId() {
        return id;
    }

    /**
     * Get the directory path
     *
     * @return The directory
     */
    public final @NotNull String getDirectory() {
        return directory;
    }

}
