package me.phoenixra.atumconfig.api.utils;

import me.phoenixra.atumconfig.api.ConfigManager;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class FileUtils {
    private FileUtils() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    /**
     * Lists all resource paths under the given directory on the classpath.
     * Supports both exploded directories and JAR packaging.
     *
     * @param configManager the ConfigManager for logging
     * @param dir           path to resource folder, relative (e.g. "configs/defaults"), as a Path
     * @return a set of classpath-relative resource strings
     */
    public static Set<String> getAllPathsInResourceFolder(
            @NotNull ConfigManager configManager,
            @NotNull Path dir
    ) {
        Set<String> found = new LinkedHashSet<>();
        // Normalize to forward-slash form for classpath lookup
        String basePath = dir.toString().replace(File.separatorChar, '/');
        ClassLoader cl = configManager.getClass().getClassLoader();
        URL url = cl.getResource(basePath);
        if (url == null) {
            configManager.getLogger().logWarn(
                    "Resource folder not found on classpath: " + basePath
            );
            return found;
        }

        try {
            String protocol = url.getProtocol();
            if ("file".equals(protocol)) {
                // exploded directory on disk
                Path folder = Paths.get(url.toURI());
                try (Stream<Path> stream = Files.walk(folder)) {
                    stream.forEach(p -> {
                        Path rel = folder.relativize(p);
                        String relStr = rel.toString().replace(File.separatorChar, '/');
                        if (!relStr.isEmpty()) {
                            found.add(basePath + "/" + relStr);
                        }
                    });
                }

            } else if ("jar".equals(protocol)) {
                // inside JAR
                String[] parts = url.toExternalForm().split("!");
                URI jarUri = URI.create(parts[0]);
                String inside = parts[1];
                try (FileSystem fs = FileSystems.newFileSystem(jarUri, Collections.emptyMap())) {
                    Path folder = fs.getPath(inside);
                    try (Stream<Path> stream = Files.walk(folder)) {
                        stream.forEach(p -> {
                            Path rel = folder.relativize(p);
                            String relStr = rel.toString().replace(File.separatorChar, '/');
                            if (!relStr.isEmpty()) {
                                found.add(basePath + "/" + relStr);
                            }
                        });
                    }
                }

            } else {
                configManager.getLogger().logWarn(
                        "Unsupported URL protocol for resource listing: " + protocol
                );
            }
        } catch (Exception e) {
            configManager.getLogger().logError(
                    "Failed listing resources in folder: " + basePath, e
            );
        }
        return found;
    }

}
