package me.phoenixra.atumconfig.core.config;

import lombok.Getter;
import me.phoenixra.atumconfig.api.ConfigOwner;
import me.phoenixra.atumconfig.api.config.ConfigType;
import me.phoenixra.atumconfig.api.config.LoadableConfig;
import me.phoenixra.atumconfig.core.config.typehandlers.ConfigTypeHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Map;
import me.phoenixra.atumconfig.utils.Objects;

public class LoadableAtumConfig extends AtumConfig implements LoadableConfig {
    protected String subDirectoryPath;
    @Getter
    protected File file;
    public LoadableAtumConfig(ConfigOwner configOwner,
                              ConfigType type,
                              String subDirectoryPath,
                              String confName,
                              boolean forceResourceLoad) {
        super(configOwner,type);
        this.subDirectoryPath = subDirectoryPath;
        File dir = new File(configOwner.getDataFolder(), subDirectoryPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        file = new File(dir, confName + "." + type.getFileExtension());
        if (!file.exists()) {
            createFile(forceResourceLoad);
        }else if(shouldUpdateConfig()){
            file.delete();
            createFile(forceResourceLoad);
        }
        try {
            reload();
            configOwner.getConfigManager().addConfig(this);
        }catch (IOException e){
            getConfigOwner().logError(Arrays.toString(e.getStackTrace()));

        }
    }
    public LoadableAtumConfig(ConfigOwner configOwner, File file) {
        this(configOwner,ConfigType.fromFile(file),file.getPath()
                .replace(configOwner.getDataFolder().getPath(),"")
                .replace(file.getName(),""),
            file.getName().split("\\.")[0],
            false
        );
    }

    public boolean shouldUpdateConfig() {
        try(InputStream inputStream = getConfigOwner().getClass().getResourceAsStream(getResourcePath())) {
            if (inputStream == null) {
                return false;
            }
            Map<String,Object> mapFromJar = ConfigTypeHandler.toMap(getConfigOwner(),
                getType(),
                ConfigTypeHandler.toString(inputStream)
            );
            //json somehow sees the integer as double
            int requiredVersion = (int) Double.parseDouble(
                Objects.requireNonNullElse(mapFromJar.get("config_version"),
                    0).toString()
            );
            InputStreamReader reader = new InputStreamReader(file.toURI().toURL().openStream());
            String s = ConfigTypeHandler.readToString(reader);
            Map<String, Object> mapFromDir = ConfigTypeHandler.toMap(getConfigOwner(), getType(), s);
            int currentVersion = (int) Double.parseDouble(
                Objects.requireNonNullElse(mapFromDir.get("config_version"),
                    0).toString()
            );

            return requiredVersion != currentVersion;
        }catch (Exception e){
            getConfigOwner().logError(Arrays.toString(e.getStackTrace()));

        }
        return false;

    }
    @Override
    public void createFile(boolean forceResourceLoad) {
        InputStream inputStream = getConfigOwner().getClass().getResourceAsStream(getResourcePath());
        if(inputStream==null && forceResourceLoad) {
            throw new NullPointerException("file not found inside the resources folder of a plugin");
        }
        if (!file.exists()) {
            try {
                if(inputStream==null) {
                    file.createNewFile();
                    return;
                }
                Files.copy(inputStream, Paths.get(file.toURI()), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                getConfigOwner().logError(Arrays.toString(e.getStackTrace()));

            }
        }
    }

    @Override
    public void reload() throws IOException {
        InputStreamReader reader = new InputStreamReader(file.toURI().toURL().openStream());
        String s = ConfigTypeHandler.readToString(reader);
        super.applyData(ConfigTypeHandler.toMap(getConfigOwner(),getType(),s));
    }

    @Override
    public void save() throws IOException {
        if(file.delete()) {
            Files.write(file.toPath(),toPlaintext().getBytes(),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE
            );
        }
    }

    @Override
    public String getResourcePath() {
        String path = subDirectoryPath.replace(" ","").isEmpty() ?
                      getFileName() : subDirectoryPath +"/"+ getFileName();
        return "/" + path;
    }

    @Override
    public String toPlaintext() {
        StringBuilder contents = new StringBuilder();
        for(String line : super.toPlaintext().split("\n")) {
            if(line.contains("\r\n")){
                for (String s : line.split("\r\n")) {
                    if(line.contains("\r")){
                        for (String s1 : line.split("\r")) {
                            if(s1.startsWith("#")) {
                                continue;
                            }
                            contents.append(s1).append("\n");
                        }
                        continue;
                    }
                    if(s.startsWith("#")) {
                        continue;
                    }
                    contents.append(s).append("\n");
                }
                continue;
            }
            if(line.contains("\r")){
                for (String s : line.split("\r")) {
                    if(line.contains("\r\n")){
                        for (String s1 : line.split("\r\n")) {
                            if(s1.startsWith("#")) {
                                continue;
                            }
                            contents.append(s1).append("\n");
                        }
                        continue;
                    }
                    if(s.startsWith("#")) {
                        continue;
                    }
                    contents.append(s).append("\n");
                }
                continue;
            }

            if(line.startsWith("#")) {
                continue;
            }
            contents.append(line).append("\n");
        }
        return contents.toString();
    }
}
