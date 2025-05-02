package me.phoenixra.atumconfig.tests.helpers;

import me.phoenixra.atumconfig.api.config.ConfigType;

public class TestHelper {
    public static final ConfigType CONFIG_TYPE;
    public static final ConfigType BAD_CONFIG_TYPE;
    public static final String FILE_EXT;
    public static final String BAD_FILE_EXT;

    static {

        String cfg = System.getProperty("test.configType", "JSON").toUpperCase();
        CONFIG_TYPE = ConfigType.valueOf(cfg);
        BAD_CONFIG_TYPE = (CONFIG_TYPE == ConfigType.JSON ? ConfigType.YAML : ConfigType.JSON);

        FILE_EXT = "." + CONFIG_TYPE.getFileExtension();
        BAD_FILE_EXT = "." + BAD_CONFIG_TYPE.getFileExtension();

        System.out.println("===========================");
        System.out.println("Starting test scenario: ");
        System.out.println("- ConfigType: "+CONFIG_TYPE.name());
        System.out.println("- Bad ConfigType: "+BAD_CONFIG_TYPE.name());
        System.out.println("===========================");
    }
}
