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
        var parser = configOwner.getConfigParser(input.getClass());
        if(parser.isPresent()){
            return parser.get()
                    .toConfig(
                            input,
                            new AtumConfigSection(
                                    configOwner,
                                    type,
                                    null
                            )
                    );
        } else if(input instanceof Map){
            return new AtumConfigSection(configOwner,type,normalizeToConfig(configOwner,type, (Map<?,?>) input));
        }else if(input instanceof Iterable){
            Iterator<?> iterator = ((Iterable<?>) input).iterator();
            if(!iterator.hasNext()) return new ArrayList<>();
            Object first = iterator.next();
            if(first == null){
                return new ArrayList<>();
            }
            else if(first instanceof Map){
                Iterable<Map<Object,Object>> iterable = (Iterable<Map<Object,Object>>) input;
                List<AtumConfigSection> building = new ArrayList<>();
                for(Map<Object,Object> map : iterable){
                    building.add(new AtumConfigSection(configOwner, type,normalizeToConfig(configOwner,type, map)));
                }
                return building;
            }
            else {
                List<Object> building = new ArrayList<>();
                for(Object obj : (Iterable<?>) input){
                    building.add(obj);
                }
                return building;
            }
        }
        return input;
    }

    public static Map<String,Object> normalizeToConfig(ConfigManager configOwner, @NotNull ConfigType type, @NotNull Map<?,?> map) {
        Map<String,Object> building = new HashMap<>();
        for(Map.Entry<?,?> entry : map.entrySet()){
            if(entry.getKey() == null || entry.getValue() == null){
                continue;
            }
            String key = entry.getKey().toString();
            Object value = entry.getValue();
            value = constrainConfigTypes(configOwner,type, value);
            building.put(key, value);
        }
        return building;
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
