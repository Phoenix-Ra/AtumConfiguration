package me.phoenixra.atumconfig.tests.helpers;

import me.phoenixra.atumconfig.api.config.ConfigType;

public class TestHelper {
    public static final ConfigType BAD_CONFIG_TYPE;
    public static final ConfigType CONFIG_TYPE;
    public static final String FILE_EXT;
    public static final String BAD_FILE_EXT;

    static {
        CONFIG_TYPE = ConfigType.YAML;
        BAD_CONFIG_TYPE = ConfigType.JSON;


        FILE_EXT = "."+CONFIG_TYPE.getFileExtension();
        BAD_FILE_EXT = "."+BAD_CONFIG_TYPE.getFileExtension();
    }
}
