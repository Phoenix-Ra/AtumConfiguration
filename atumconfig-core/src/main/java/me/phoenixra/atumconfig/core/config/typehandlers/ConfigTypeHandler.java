package me.phoenixra.atumconfig.core.config.typehandlers;


import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.api.config.ConfigParser;
import me.phoenixra.atumconfig.api.config.ConfigType;
import me.phoenixra.atumconfig.core.config.AtumConfigSection;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

import static me.phoenixra.atumconfig.core.config.typehandlers.ConfigTypeHandlers.HANDLERS;

public abstract class ConfigTypeHandler {


    private ConfigType type;
    public ConfigTypeHandler(ConfigType type) {
        this.type = type;
    }
    public Map<String,Object> toMap(ConfigManager configOwner, String input) {
        if (input == null || input.replace(" ", "").isEmpty()){
            return new HashMap<>();
        }

        return normalizeToConfig(configOwner, type,parseToMap(input));
    }
    protected abstract Map<String,Object> parseToMap(String input);
    public abstract String toString(Map<String,Object> map);


    public static Map<String,Object> toMap(ConfigManager configOwner,@NotNull ConfigType type, @NotNull String input) {
        return HANDLERS.get(type).toMap(configOwner,input);
    }
    public static String toString(@NotNull ConfigType type, @NotNull Map<String,Object> map) {
        return HANDLERS.get(type).toString(map);
    }
    public static String toString(InputStream inputStream) throws IOException {
        //creating an InputStreamReader object
        InputStreamReader isReader = new InputStreamReader(inputStream);
        //Creating a BufferedReader object
        BufferedReader reader = new BufferedReader(isReader);
        StringBuffer sb = new StringBuffer();
        String str;
        while ((str = reader.readLine()) != null) {
            sb.append(str);
        }
        return sb.toString();
    }
    public static Object constrainConfigTypes(ConfigManager configOwner, @NotNull ConfigType type, Object input) {
        if (input == null) {
            return null;
        }

        // 1) If there’s a parser for this exact input class, use it:
        var parser = configOwner.getConfigParser(input.getClass());
        if (parser.isPresent()) {
            AtumConfigSection section = new AtumConfigSection(configOwner, type, null);
            return parser.get().toConfig(input, section);
        }

        // 2) If it’s a raw Map, normalize its entries and wrap in a section:
        if (input instanceof Map<?,?> rawMap) {
            Map<String,Object> normalized = normalizeToConfig(configOwner, type, rawMap);
            return new AtumConfigSection(configOwner, type, normalized);
        }

        // 3) If it’s any Iterable, process each element recursively:
        if (input instanceof Iterable<?> iterable) {
            List<Object> list = new ArrayList<>();
            for (Object elem : iterable) {
                list.add(constrainConfigTypes(configOwner, type, elem));
            }
            return list;
        }

        // 4) Otherwise, leave it alone (primitives, strings, etc.)
        return input;
    }

    public static Map<String,Object> normalizeToConfig(ConfigManager configOwner, @NotNull ConfigType type, @NotNull Map<?,?> raw) {
        Map<String,Object> out = new LinkedHashMap<>();
        for (Map.Entry<?,?> entry : raw.entrySet()) {
            String key = entry.getKey().toString();
            Object val = entry.getValue();
            out.put(key, constrainConfigTypes(configOwner, type, val));
        }
        return out;
    }
    public static String readToString(@NotNull Reader input) {

        try(BufferedReader reader = input instanceof BufferedReader ? (BufferedReader)input : new BufferedReader(input)) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }
            return builder.toString();
        }catch (Exception e){
            throw new RuntimeException(e);
        }

    }
}
