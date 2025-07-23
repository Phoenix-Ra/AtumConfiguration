package me.phoenixra.atumconfig.core.config;

import lombok.Getter;
import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.atumconfig.api.config.ConfigType;
import me.phoenixra.atumconfig.api.config.catalog.ConfigCatalog;
import me.phoenixra.atumconfig.api.config.catalog.ConfigCatalogListener;
import me.phoenixra.atumconfig.api.utils.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;


@Getter
public class AtumConfigCatalog implements ConfigCatalog {
    private final ConfigManager configManager;
    private final ConfigType type;
    private final String id;
    private final Path directory;
    private final ConfigCatalogListener listener;
    private final boolean nestedDirectories;

    protected Map<String, ConfigFile> configFilesMap = new ConcurrentHashMap<>();


    public AtumConfigCatalog(@NotNull ConfigManager configManager,
                             @NotNull ConfigType type,
                             @NotNull String id,
                             @NotNull Path relativeDirectory,
                             boolean nested,
                             @NotNull ConfigCatalogListener catalogListener) {
        this.configManager = configManager;
        this.type = type;
        this.id = id;
        this.directory = relativeDirectory;
        this.listener = catalogListener;
        this.nestedDirectories = nested;
    }


    @Override
    public void reload() {

        listener.beforeReload(this);
        listener.onClear(this);
        configFilesMap.clear();

        Path baseDir = configManager.getDirectory().resolve(directory);
        configManager.getLogger().logInfo("Reloading catalog '" + id + "' from " + baseDir);

        // Ensure defaults present
        if (Files.notExists(baseDir)) {
            loadDefaults();
        }

        try (Stream<Path> stream = nestedDirectories ? Files.walk(baseDir) : Files.list(baseDir)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(type.getFileExtension()))
                    .forEach(path -> {
                        // Compute relative path without extension
                        Path relParent = baseDir.relativize(path.getParent());
                        String filename = path.getFileName().toString();
                        int dot = filename.lastIndexOf('.');
                        String nameNoExt = dot == -1 ? filename : filename.substring(0, dot);

                        // Build ID (relative/path/filename)
                        Path idPath = relParent.resolve(nameNoExt);
                        String configId = idPath.toString().replace(File.separatorChar, '/');

                        // Path to the file including extension
                        Path relativeFile = directory.resolve(relParent).resolve(path.getFileName());

                        try {
                            ConfigFile conf = new AtumConfigFile(
                                    configManager,
                                    type,
                                    configId,
                                    relativeFile,
                                    false
                            );
                            configFilesMap.put(configId, conf);
                            listener.onConfigLoaded(this, conf);
                        } catch (Throwable e) {
                            configManager.getLogger().logError(
                                    "Failed to load config '" + filename + "' in catalog '" + id + "'", e
                            );
                            listener.onConfigFailed(this, relativeFile, e);
                        }
                    });
        } catch (IOException e) {
            configManager.getLogger().logError("Failed scanning catalog directory: " + baseDir, e);
        } finally {
            listener.afterReload(this);
        }
    }

    private void loadDefaults() {
        listener.beforeLoadDefaults(this);
        Path root = configManager.getDirectory();
        for (String resourcePath : FileUtils.getAllPathsInResourceFolder(configManager, directory)) {
            Path target = root.resolve(resourcePath);
            try {
                if (!resourcePath.contains(".")) {
                    // treat as directory
                    Files.createDirectories(target);
                } else {
                    // treat as file
                    try (InputStream in = Thread.currentThread()
                            .getContextClassLoader()
                            .getResourceAsStream(resourcePath)) {
                        if (in == null) {
                            configManager.getLogger().logWarn("Default resource not found: " + resourcePath);
                            continue;
                        }
                        Files.createDirectories(target.getParent());
                        configManager.getLogger().logInfo("Copying default resource: " + resourcePath);
                        Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            } catch (IOException e) {
                configManager.getLogger().logError("Error loading default for resource: " + resourcePath, e);
            }
        }
        listener.afterLoadDefaults(this);

        Path baseDir = configManager
                .getDirectory().resolve(directory);
        if (Files.notExists(baseDir)) {
            try {
                Files.createDirectory(baseDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public @NotNull Optional<ConfigFile> getConfigFile(@NotNull String id) {
        return Optional.ofNullable(configFilesMap.get(id));
    }

}
