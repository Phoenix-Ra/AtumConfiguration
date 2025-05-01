package me.phoenixra.atumconfig.tests;

import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumconfig.api.config.ConfigType;
import me.phoenixra.atumconfig.core.AtumConfigManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigTest {

    @TempDir
    Path tmpRoot;
    private ConfigManager cm;

    @BeforeEach
    void setUp() {
        cm = new AtumConfigManager("test", tmpRoot, true);
    }

    @Test
    void testInitialDataRoundTrip() {
        Map<String, Object> initial = Map.of("a", 1, "b", "two", "flag", true);
        Config cfg = cm.createConfig(ConfigType.JSON, initial);

        assertEquals(1,    cfg.getInt("a"));
        assertEquals("two",cfg.getString("b"));
        assertTrue(        cfg.getBool("flag"));
    }

    @Test
    void testToPlaintextAndFromString() {
        Config c1 = cm.createConfig(ConfigType.JSON, null);
        c1.set("x", 42);
        c1.set("nested.name", "foo");
        String raw = c1.toPlaintext();

        Config c2 = cm.createConfigFromString(ConfigType.JSON, raw);
        assertEquals(42,          c2.getInt("x"));
        assertEquals("foo",       c2.getString("nested.name"));
    }

    @Test
    void testInvalidStringRejection() {
        String bad = "{ not valid JSON ";
        assertThrows(RuntimeException.class, () ->
                cm.createConfigFromString(ConfigType.JSON, bad)
        );
    }

    @Test
    void testTypeSafeGetters() {
        String json = """
            {
              "i": 7,
              "d": 3.14,
              "l": 1234567890123,
              "flag": false,
              "list": [1, 2, 3]
            }
            """;
        Config cfg = cm.createConfigFromString(ConfigType.JSON, json);

        assertEquals(7,                       cfg.getInt("i"));
        assertEquals(3.14,                    cfg.getDouble("d"));
        assertEquals(1234567890123L,          cfg.getLong("l"));
        assertFalse(cfg.getBool("flag"));
        List<Integer> list = cfg.getIntList("list");
        assertTrue(list.get(0) == 1 && list.get(1) == 2  && list.get(2) == 3 );

        // defaults
        assertEquals(123,   cfg.getIntOrDefault("missingInt", 123));
        assertFalse(cfg.getBool("noFlag"));
        assertEquals(List.of(), cfg.getStringList("noList"));
    }
}
