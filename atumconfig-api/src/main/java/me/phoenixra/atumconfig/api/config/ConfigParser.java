package me.phoenixra.atumconfig.api.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ConfigParser<T> {

    @Nullable
    Config toConfig(Object value, Config config);

    @Nullable
    T fromConfig(Config config);

    @NotNull
    Class<T> getClassParsed();
}
