package me.phoenixra.atumconfig.tests;

import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumconfig.core.AtumConfigManager;
import me.phoenixra.atumconfig.tests.helpers.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.*;

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

        Map<String, Object> initial = new HashMap<>();
        initial.put("a",1);
        initial.put("b","two");
        initial.put("flag",true);
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
        assertEquals(Arrays.asList(1, 2, 3), list);

        // defaults
        assertEquals(123,   cfg.getIntOrDefault("missingInt", 123));
        assertFalse(cfg.getBool("noFlag"));
        assertEquals(Collections.emptyList(), cfg.getStringList("noList"));
    }

    //――――――――――――――――――――――――――――――――――――――――――――――
    // helpers
    //――――――――――――――――――――――――――――――――――――――――――――――

    private static String getInvalidRaw() {
        switch (TestHelper.CONFIG_TYPE) {
            case JSON:
                return "{ not valid JSON ";
            case YAML:
                return "not valid: [unbalanced";
            default:
                throw new IllegalStateException("Unsupported: " + TestHelper.CONFIG_TYPE);
        }
    }

    private static String getTypeSafeRaw() {
        String raw;
        switch (TestHelper.CONFIG_TYPE) {
            case JSON:
                raw = "{\n"
                        + "  \"i\": 7,\n"
                        + "  \"d\": 3.14,\n"
                        + "  \"l\": 1234567890123,\n"
                        + "  \"flag\": false,\n"
                        + "  \"list\": [1, 2, 3]\n"
                        + "}\n";
                break;
            case YAML:
                raw = "i: 7\n"
                        + "d: 3.14\n"
                        + "l: 1234567890123\n"
                        + "flag: false\n"
                        + "list:\n"
                        + "  - 1\n"
                        + "  - 2\n"
                        + "  - 3\n";
                break;
            default:
                throw new IllegalStateException("Unsupported: " + TestHelper.CONFIG_TYPE);
        }
        return raw;
    }



}
