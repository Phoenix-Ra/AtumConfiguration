package me.phoenixra.atumconfig.core.config;

import lombok.Getter;
import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.api.config.ConfigType;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.atumconfig.core.config.typehandlers.ConfigTypeHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

public class AtumConfigFile extends AtumConfig implements ConfigFile {
    @Getter
    private String id;
    @Getter
    private Path relativeFilePath;
    private Path absolutePath;

    @Getter
    protected File file;


    public AtumConfigFile(@NotNull ConfigManager configOwner,
                          @NotNull ConfigType type,
                          @NotNull String id,
                          @NotNull Path relativePath,
                          boolean forceLoadResource) throws IOException{
        super(configOwner,type);

        this.id = id;
        this.relativeFilePath = relativePath;
        this.absolutePath = configOwner.getDirectory().resolve(relativePath).normalize();

        // Ensure parent directories exist
        Path parent = absolutePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        file = absolutePath.toFile();
        if (!file.exists()) {
            createFile(forceLoadResource);
        }
        reload();
    }

    @Override
    public void createFile(boolean forceLoadResource) {
        if (forceLoadResource || !file.exists()) {
            String resourcePath = getResourcePath();
            try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
                if (in == null && forceLoadResource) {
                    throw new IOException("Resource not found: " + resourcePath);
                }else if(in != null) {
                    Files.copy(in, absolutePath, StandardCopyOption.REPLACE_EXISTING);
                }else{
                    Files.createFile(absolutePath);
                }
            }catch (Exception e){
                getConfigOwner().getLogger().logError(
                        "Exception while creating config file "+getId(),
                        e
                );
            }
        }
    }

    @Override
    public void reload() throws IOException {
        byte[] bytes = Files.readAllBytes(absolutePath);
        String raw = new String(bytes, StandardCharsets.UTF_8);
        Map<String, Object> data = ConfigTypeHandler.toMap(getConfigOwner(), getType(), raw);
        super.applyData(data);
    }

    @Override
    public void save() throws IOException {
        Files.write(
                absolutePath,
                toPlaintext().getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    @Override
    public @NotNull String getResourcePath() {
        // Use forward-slash separators internally for classpath lookup
        String unixPath = relativeFilePath.toString().replace('\\', '/');
        return "/" + unixPath;
    }

    @Override
    public String toPlaintext() {
        StringBuilder sb = new StringBuilder();
        for (String line : super.toPlaintext().split("\\r?\\n")) {
            sb.append(line).append('\n');
        }
        return sb.toString();
    }
}
