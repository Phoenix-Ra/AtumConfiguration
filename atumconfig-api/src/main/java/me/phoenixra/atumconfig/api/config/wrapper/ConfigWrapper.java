package me.phoenixra.atumconfig.api.config.wrapper;


import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumconfig.api.ConfigOwner;
import me.phoenixra.atumconfig.api.config.ConfigType;
import me.phoenixra.atumconfig.api.placeholders.InjectablePlaceholder;
import me.phoenixra.atumconfig.api.placeholders.context.PlaceholderContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class ConfigWrapper<T extends Config> implements Config {

    private final T handle;

    /**
     * Create a config wrapper.
     *
     * @param handle The config that is being wrapped.
     */
    protected ConfigWrapper(@NotNull final T handle) {
        this.handle = handle;
    }

    @Override
    public String toPlaintext() {
        return handle.toPlaintext();
    }

    @Override
    public boolean hasPath(@NotNull String path) {
        return handle.hasPath(path);
    }

    @Override
    public @NotNull List<String> getKeys(boolean deep) {
        return handle.getKeys(deep);
    }

    @Override
    public void applyData(Map<String, Object> values) {
        handle.applyData(values);
    }

    @Override
    public @Nullable Object get(@NotNull String path) {
        return handle.get(path);
    }

    @Override
    public void set(@NotNull String path, @Nullable Object obj) {
        handle.set(path,obj);
    }

    @Override
    public @Nullable Byte getByteOrNull(@NotNull String path) {
        return handle.getByteOrNull(path);
    }

    @Override
    public @Nullable List<Byte> getByteListOrNull(@NotNull String path) {
        return handle.getByteListOrNull(path);
    }

    @Override
    public @Nullable Short getShortOrNull(@NotNull String path) {
        return handle.getShortOrNull(path);
    }

    @Override
    public @Nullable List<Short> getShortListOrNull(@NotNull String path) {
        return handle.getShortListOrNull(path);
    }

    @Override
    public @Nullable Integer getIntOrNull(@NotNull String path) {
        return handle.getIntOrNull(path);
    }

    @Override
    public @Nullable List<Integer> getIntListOrNull(@NotNull String path) {
        return handle.getIntListOrNull(path);
    }

    @Override
    public @Nullable Long getLongOrNull(@NotNull String path) {
        return handle.getLongOrNull(path);
    }

    @Override
    public @Nullable List<Long> getLongListOrNull(@NotNull String path) {
        return handle.getLongListOrNull(path);
    }

    @Override
    public @Nullable Boolean getBoolOrNull(@NotNull String path) {
        return handle.getBoolOrNull(path);
    }

    @Override
    public @Nullable List<Boolean> getBoolListOrNull(@NotNull String path) {
        return handle.getBoolListOrNull(path);
    }

    @Override
    public @Nullable String getStringOrNull(@NotNull String path) {
        return handle.getStringOrNull(path);
    }

    @Override
    public @Nullable List<String> getStringListOrNull(@NotNull String path) {
        return handle.getStringListOrNull(path);
    }

    @Override
    public @Nullable Float getFloatOrNull(@NotNull String path) {
        return handle.getFloatOrNull(path);
    }

    @Override
    public @Nullable List<Float> getFloatListOrNull(@NotNull String path) {
        return handle.getFloatListOrNull(path);
    }
    @Override
    public @Nullable Double getDoubleOrNull(@NotNull String path) {
        return handle.getDoubleOrNull(path);
    }

    @Override
    public @Nullable List<Double> getDoubleListOrNull(@NotNull String path) {
        return handle.getDoubleListOrNull(path);
    }

    @Override
    public @NotNull Config getSubsectionOrNull(@NotNull String path) {
        return handle.getSubsection(path);
    }

    @Override
    public @Nullable List<? extends Config> getSubsectionListOrNull(@NotNull String path) {
        return handle.getSubsectionListOrNull(path);
    }

    @Override
    public double getEvaluated(@NotNull String path, @NotNull PlaceholderContext context) {
        return handle.getEvaluated(path,context);
    }


    @Override
    public @NotNull ConfigType getType() {
        return handle.getType();
    }

    @Override
    public ConfigOwner getConfigOwner() {
        return handle.getConfigOwner();
    }

    public T getHandle(){
        return handle;
    }

    @Override
    public void addInjectablePlaceholder(@NotNull Iterable<InjectablePlaceholder> placeholders, boolean deep) {
        handle.addInjectablePlaceholder(placeholders,deep);
    }

    @Override
    public void removeInjectablePlaceholder(@NotNull Iterable<InjectablePlaceholder> placeholders, boolean deep) {
        handle.removeInjectablePlaceholder(placeholders,deep);
    }

    @Override
    public void clearInjectedPlaceholders(boolean deep) {
        handle.clearInjectedPlaceholders(deep);
    }

    @Override
    public @NotNull List<InjectablePlaceholder> getPlaceholderInjections() {
        return handle.getPlaceholderInjections();
    }
}
