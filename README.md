
Gradle:
```
repositories {
     ...
     maven { url 'https://jitpack.io' }
}
dependencies { 
    implementation("com.github.Phoenix-Ra:AtumConfiguration:Tag")
}
```


To start:


```

        ConfigManager configManager = new AtumConfigManager(
                "Id",
                Path.of("path/to/configs"),
                true
        );
        ConfigFile config = configManager.createConfigFile(
                ConfigType.JSON,
                "config",
                Path.of("config.json"),
                false
        );


```

Planned:
- Changable config types supported by ConfigManager (no strict bound to ConfigType enum)
- Allow custom config type parsers
- Move built-in config parsers to separate submodules, to allow including into projects only used staff
