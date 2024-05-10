package me.phoenixra.atumconfig.core.config.typehandlers;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import lombok.Getter;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumconfig.api.config.ConfigType;
import me.phoenixra.atumconfig.core.config.AtumConfigSection;

import java.lang.reflect.Type;
import java.util.Map;

public class TypeHandlerJson extends ConfigTypeHandler {
    @Getter
    private AtumGsonSerializer serializer = new AtumGsonSerializer();
    public TypeHandlerJson() {
        super(ConfigType.JSON);
    }

    @Override
    protected Map<String, Object> parseToMap(String input) {
        return serializer.gson.fromJson(input, Map.class);
    }

    @Override
    public String toString(Map<String, Object> map) {
        return serializer.gson.toJson(map);
    }


    public static class AtumGsonSerializer implements JsonSerializer<Config> {
        @Getter
        protected Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .registerTypeAdapter(Config.class, this)
                .create();


        @Override
        public JsonElement serialize(Config src, Type typeOfSrc, JsonSerializationContext context) {
            return gson.toJsonTree(src.toMap());
        }

    }
}
