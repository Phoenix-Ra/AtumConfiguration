
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
- Changable config types supported by ConfigManager, allow custom config type parsers.
