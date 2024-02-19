package me.phoenixra.atumconfig.core.config;

import lombok.Getter;
import me.phoenixra.atumconfig.api.ConfigOwner;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumconfig.api.config.ConfigType;
import me.phoenixra.atumconfig.api.placeholders.InjectablePlaceholder;
import me.phoenixra.atumconfig.api.placeholders.PlaceholderManager;
import me.phoenixra.atumconfig.api.placeholders.context.PlaceholderContext;
import me.phoenixra.atumconfig.core.config.typehandlers.ConfigTypeHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redempt.crunch.Crunch;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AtumConfig implements Config {
    @Getter
    protected ConfigOwner configOwner;
    protected ConfigType configType;

    protected List<InjectablePlaceholder> injections = Collections.synchronizedList(new ArrayList<>());

    protected ConcurrentHashMap<String, Object> values = new ConcurrentHashMap<>();

    public AtumConfig(ConfigOwner configOwner, ConfigType configType, Map<String, Object> values) {
        this.configOwner = configOwner;
        this.configType = configType;
        this.values.putAll(values);
    }
    public AtumConfig(ConfigOwner configOwner, ConfigType configType) {
        this(configOwner, configType, new ConcurrentHashMap<>());
    }
    protected void applyData(Map<String, Object> values){
        this.values.clear();
        this.values.putAll(values);
    }

    @Override
    public @NotNull List<String> getKeys(boolean deep) {
        return deep ? recurseKeys(new HashSet<>(),"") : new ArrayList<>(values.keySet());
    }

    @Override
    public @NotNull List<String> recurseKeys(@NotNull Set<String> current, @NotNull String root) {
        Set<String> list = new HashSet<>();
        for (String key : getKeys(false)) {
            list.add(root+key);
            Object found = get(key);

            if (found instanceof Config) {
                list.addAll(((Config) found).recurseKeys(current, root+key+"."));
            }

        }
        return new ArrayList<>(list);
    }

    @Override
    public @Nullable Object get(@NotNull String path) {
        String nearestPath = path.split("\\.")[0];
        if(path.contains(".")){
            String remainingPath = path.replaceFirst(nearestPath+"\\.","");
            if(remainingPath.isEmpty()){
                return null;
            }
            Object first = get(nearestPath);
            if(first instanceof Config){
                return ((Config) first).get(remainingPath);
            }
            return null;
        }
        return values.get(nearestPath);
    }

    @Override
    public void set(@NotNull String path, @Nullable Object obj) {
        String nearestPath = path.split("\\.")[0];
        if(path.contains(".")){
            String remainingPath = path.replaceFirst(nearestPath+"\\.","");
            if(remainingPath.isEmpty()){
                return;
            }
            Config section = getSubsectionOrNull(nearestPath);
            if(section==null){
                section = new AtumConfig(getConfigOwner(),configType);
            }
            section.set(remainingPath, obj);
            values.put(nearestPath, section);
            return;
        }
        if(obj == null){
            values.remove(nearestPath);
        }else{
            values.put(nearestPath, ConfigTypeHandler.constrainConfigTypes(configOwner,configType,obj));
        }
    }

    @Override
    public String toPlaintext() {
        return ConfigTypeHandler.toString(configType,this.values);
    }

    @Override
    public boolean hasPath(@NotNull String path) {
        return get(path) != null;
    }


    @Override
    public @Nullable Integer getIntOrNull(@NotNull String path) {
        Object obj = get(path);
        return (obj instanceof Number)? ((Number) obj).intValue() : null;
    }

    @Override
    public @Nullable List<Integer> getIntListOrNull(@NotNull String path) {
        List<Number> list = getList(path, Number.class);
        if(list == null){
            return null;
        }
        return list.stream().map(Number::intValue).collect(Collectors.toList());
    }
    @Override
    public @Nullable Double getDoubleOrNull(@NotNull String path) {
        Object obj = get(path);
        return (obj instanceof Number)? ((Number) obj).doubleValue() : null;
    }

    @Override
    public @Nullable List<Double> getDoubleListOrNull(@NotNull String path) {
        List<Number> list = getList(path, Number.class);
        if(list == null){
            return null;
        }
        return list.stream().map(Number::doubleValue).collect(Collectors.toList());
    }
    @Override
    public @Nullable Boolean getBoolOrNull(@NotNull String path) {
        Object obj = get(path);
        return (obj instanceof Boolean)? (Boolean) obj : null;
    }

    @Override
    public @Nullable List<Boolean> getBoolListOrNull(@NotNull String path) {
        return getList(path, Boolean.class);
    }

    @Override
    public @Nullable String getStringOrNull(@NotNull String path) {
        Object obj = get(path);
        return obj != null ? obj.toString() : null;
    }

    @Override
    public @Nullable List<String> getStringListOrNull(@NotNull String path) {
        List<Object> list = getList(path, Object.class);
        return list.stream().map(Object::toString).collect(Collectors.toList());
    }

    @Override
    public @Nullable Config getSubsectionOrNull(@NotNull String path) {
        Object obj = get(path);
        return (obj instanceof Config)? (Config) obj : null;
    }

    @Override
    public @Nullable List<? extends Config> getSubsectionListOrNull(@NotNull String path) {
        Config section = getSubsectionOrNull(path);
        if(section == null){
            return null;
        }
        List<Config> list = new ArrayList<>();
        for(String key : section.getKeys(false)){
            Object obj = section.get(key);
            if(obj instanceof Config){
                list.add((Config) obj);
            }
        }
        return list;
    }

    @Override
    public double getEvaluated(@NotNull String path, @NotNull PlaceholderContext context) {
        String text = getStringOrNull(path);
        if(text == null){
            return 0.0;
        }
        return Crunch.compileExpression(
                PlaceholderManager.translatePlaceholders(
                        configOwner,
                        text,
                        context
                ),
                ((AtumConfigManager)getConfigOwner().getConfigManager()).getEvaluationEnvironment()
        ).evaluate();
    }


    private<T> List<T> getList(String path, Class<T> type){

        Object obj = get(path);
        if(!(obj instanceof Iterable)){
            return null;
        }
        Iterator<?> iterator = ((Iterable<?>) obj).iterator();

        if(iterator.hasNext()){
            Object first = iterator.next();
            if(first.getClass().isAssignableFrom(type)){
                List<T> list = new ArrayList<>();
                list.add((T) first);
                while(iterator.hasNext()){
                    list.add((T) iterator.next());
                }
                return list;
            }
        }
        return new ArrayList<>();
    }

    @Override
    public void addInjectablePlaceholder(@NotNull Iterable<InjectablePlaceholder> placeholders) {
        for (InjectablePlaceholder placeholder : placeholders) {
            if (placeholder == null) {
                continue;
            }
            if (injections.contains(placeholder)) {
                continue;
            }
            injections.add(placeholder);
        }
    }

    @Override
    public void removeInjectablePlaceholder(@NotNull Iterable<InjectablePlaceholder> placeholders) {
        for (InjectablePlaceholder placeholder : placeholders) {
            if (placeholder == null) {
                continue;
            }
            injections.remove(placeholder);
        }
    }

    @Override
    public void clearInjectedPlaceholders() {
        injections.clear();
    }
    @Override
    public @NotNull List<InjectablePlaceholder> getPlaceholderInjections() {
        return injections;
    }

    @Override
    public @NotNull ConfigType getType() {
        return configType;
    }

    @Override
    public Map<String, Object> toMap() {
        return values;
    }

}
