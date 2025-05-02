package me.phoenixra.atumconfig.tests;

import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumconfig.core.AtumConfigManager;
import me.phoenixra.atumconfig.tests.helpers.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * As an experiment, I mostly generated these tests by AI
 * It looks a little bit messy, but covers many cases.
 */
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
        Config cfg = cm.createConfig(TestHelper.CONFIG_TYPE, initial);

        assertEquals(1,    cfg.getInt("a"));
        assertEquals("two",cfg.getString("b"));
        assertTrue(        cfg.getBool("flag"));
    }

    @Test
    void testToPlaintextAndFromString() {
        Config c1 = cm.createConfig(TestHelper.CONFIG_TYPE, null);
        c1.set("x", 42);
        c1.set("nested.name", "foo");
        String raw = c1.toPlaintext();

        Config c2 = cm.createConfigFromString(TestHelper.CONFIG_TYPE, raw);
        assertEquals(42,    c2.getInt("x"));
        assertEquals("foo", c2.getString("nested.name"));
    }

    @Test
    void testInvalidStringRejection() {
        String bad = getInvalidRaw();
        assertThrows(RuntimeException.class, () ->
                cm.createConfigFromString(TestHelper.CONFIG_TYPE, bad)
        );
    }

    @Test
    void testTypeSafeGetters() {
        String raw = getTypeSafeRaw();
        Config cfg = cm.createConfigFromString(TestHelper.CONFIG_TYPE, raw);

        assertEquals(7,                      cfg.getInt("i"));
        assertEquals(3.14,                   cfg.getDouble("d"));
        assertEquals(1234567890123L,         cfg.getLong("l"));
        assertFalse(cfg.getBool("flag"));
        List<Integer> list = cfg.getIntList("list");
        assertEquals(List.of(1, 2, 3), list);

        // defaults
        assertEquals(123,   cfg.getIntOrDefault("missingInt", 123));
        assertFalse(cfg.getBool("noFlag"));
        assertEquals(List.of(), cfg.getStringList("noList"));
    }

    //――――――――――――――――――――――――――――――――――――――――――――――
    // helpers
    //――――――――――――――――――――――――――――――――――――――――――――――

    private static String getInvalidRaw() {
        return switch (TestHelper.CONFIG_TYPE) {
            case JSON -> "{ not valid JSON ";
            case YAML -> "not valid: [unbalanced";
            default    -> throw new IllegalStateException("Unsupported: " + TestHelper.CONFIG_TYPE);
        };
    }

    private static String getTypeSafeRaw() {
        return switch (TestHelper.CONFIG_TYPE) {
            case JSON -> """
                {
                  "i": 7,
                  "d": 3.14,
                  "l": 1234567890123,
                  "flag": false,
                  "list": [1, 2, 3]
                }
                """;
            case YAML -> """
                i: 7
                d: 3.14
                l: 1234567890123
                flag: false
                list:
                  - 1
                  - 2
                  - 3
                """;
            default    -> throw new IllegalStateException("Unsupported: " + TestHelper.CONFIG_TYPE);
        };
    }



}
