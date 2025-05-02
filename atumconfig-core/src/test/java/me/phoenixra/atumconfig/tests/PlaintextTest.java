package me.phoenixra.atumconfig.tests;

import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.atumconfig.api.config.ConfigType;
import me.phoenixra.atumconfig.core.AtumConfigManager;
import me.phoenixra.atumconfig.tests.helpers.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * As an experiment, I mostly generated these tests by AI
 * It looks a little bit messy, but covers many cases.
 */
public class PlaintextTest {
    @TempDir Path tmpRoot;
    private ConfigManager manager;

    @BeforeEach
    void setUp() {
        manager = new AtumConfigManager("test", tmpRoot, true);
    }

    private static boolean isJson() {
        return TestHelper.CONFIG_TYPE == ConfigType.JSON;
    }

    private static String keyToken(String key) {
        return isJson() ? "\"" + key + "\"" : key + ":";
    }

    private static void writeOrderedExample(Path file) throws IOException {
        String content;
        if (isJson()) {
            content = """
                {
                  "first": true,
                  "second": "two",
                  "third": 3
                }
                """;
        } else {
            content = """
                first: true
                second: "two"
                third: 3
                """;
        }
        Files.writeString(file, content);
    }

    @Test
    void inMemoryConfig_toPlaintext_roundTripsAndPreservesOrder() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("alpha", "A");
        data.put("beta", 123);
        data.put("gamma", false);

        Config cfg = manager.createConfig(TestHelper.CONFIG_TYPE, data);
        String plain = cfg.toPlaintext();
        assertNotNull(plain);

        Config reparsed = manager.createConfigFromString(TestHelper.CONFIG_TYPE, plain);
        assertEquals("A",    reparsed.getString("alpha"));
        assertEquals(123,    reparsed.getInt("beta"));
        assertFalse(        reparsed.getBool("gamma"));

        List<String> keys = reparsed.getKeys(false);
        assertEquals(List.of("alpha", "beta", "gamma"), keys);

        int iA = plain.indexOf(keyToken("alpha"));
        int iB = plain.indexOf(keyToken("beta"));
        int iC = plain.indexOf(keyToken("gamma"));
        assertTrue(iA >= 0 && iB > iA && iC > iB,
                "Expected alpha < beta < gamma in the output");
    }

    @Test
    void configFile_toPlaintext_roundTripsAndPreservesOrder() throws IOException {
        Path f = tmpRoot.resolve("settings" + TestHelper.FILE_EXT);
        writeOrderedExample(f);

        ConfigFile file = manager.createConfigFile(
                TestHelper.CONFIG_TYPE,
                "settings",
                Path.of("settings" + TestHelper.FILE_EXT),
                false
        );

        String out = file.toPlaintext();
        assertNotNull(out);

        Config round = manager.createConfigFromString(TestHelper.CONFIG_TYPE, out);
        assertTrue(round.getBool("first"));
        assertEquals("two", round.getString("second"));
        assertEquals(3,      round.getInt("third"));

        List<String> keys = round.getKeys(false);
        assertEquals(List.of("first", "second", "third"), keys);

        int i1 = out.indexOf(keyToken("first"));
        int i2 = out.indexOf(keyToken("second"));
        int i3 = out.indexOf(keyToken("third"));
        assertTrue(i1 >= 0 && i2 > i1 && i3 > i2,
                "Expected first < second < third in the serialized output");
    }

    @Test
    void nestedObjects_orderPreserved() {
        var innerA = new LinkedHashMap<String, Object>();
        innerA.put("x", 1);
        innerA.put("y", 2);
        var innerB = new LinkedHashMap<String, Object>();
        innerB.put("m", 3);
        innerB.put("n", 4);

        var data = new LinkedHashMap<String, Object>();
        data.put("a", innerA);
        data.put("b", innerB);

        Config cfg = manager.createConfig(TestHelper.CONFIG_TYPE, data);
        String plain = cfg.toPlaintext();
        Config round = manager.createConfigFromString(TestHelper.CONFIG_TYPE, plain);

        assertEquals(List.of("a", "b"), round.getKeys(false));
        assertEquals(List.of("x", "y"), round.getSubsection("a").getKeys(false));
        assertEquals(List.of("m", "n"), round.getSubsection("b").getKeys(false));
    }

    @Test
    void primitiveAndObjectLists_orderPreserved() {
        var data = new LinkedHashMap<String, Object>();
        data.put("primList", List.of("one", "two", "three"));

        var o1 = Map.<String, Object>of("id", "i1");
        var o2 = Map.<String, Object>of("id", "i2");
        data.put("objList", List.of(o1, o2));

        Config cfg = manager.createConfig(TestHelper.CONFIG_TYPE, data);
        String plain = cfg.toPlaintext();
        Config round = manager.createConfigFromString(TestHelper.CONFIG_TYPE, plain);

        assertEquals(List.of("one","two","three"), round.getStringList("primList"));
        var objs = round.getSubsectionList("objList");
        assertEquals(2, objs.size());
        assertEquals("i1", objs.get(0).getString("id"));
        assertEquals("i2", objs.get(1).getString("id"));
    }

    @Test
    void nullRemoval_keyExcludedInPlaintext() {
        var data = new LinkedHashMap<String, Object>();
        data.put("keep", "v1");
        data.put("remove", "v2");

        Config cfg = manager.createConfig(TestHelper.CONFIG_TYPE, data);
        cfg.set("remove", null);

        String plain = cfg.toPlaintext();
        assertTrue(plain.contains(keyToken("keep")));
        assertFalse(plain.contains(keyToken("remove")));
    }

    @Test
    void updateDoesNotReorderExistingKeys() {
        var data = new LinkedHashMap<String, Object>();
        data.put("k1", 1);
        data.put("k2", 2);
        data.put("k3", 3);

        Config cfg = manager.createConfig(TestHelper.CONFIG_TYPE, data);
        cfg.set("k2", 22);

        String plain = cfg.toPlaintext();
        Config round = manager.createConfigFromString(TestHelper.CONFIG_TYPE, plain);

        assertEquals(List.of("k1","k2","k3"), round.getKeys(false));

        int i1 = plain.indexOf(keyToken("k1"));
        int i2 = plain.indexOf(keyToken("k2"));
        int i3 = plain.indexOf(keyToken("k3"));
        assertTrue(i1 >= 0 && i2 > i1 && i3 > i2);
    }

    @Test
    void appendingNewKeys_appendsAtEnd() {
        var data = new LinkedHashMap<String, Object>();
        data.put("k1", 1);
        data.put("k2", 2);
        data.put("k3", 3);

        Config cfg = manager.createConfig(TestHelper.CONFIG_TYPE, data);
        cfg.set("k4", 4);

        String plain = cfg.toPlaintext();
        Config round = manager.createConfigFromString(TestHelper.CONFIG_TYPE, plain);

        assertEquals(List.of("k1","k2","k3","k4"), round.getKeys(false));
        int i3 = plain.indexOf(keyToken("k3"));
        int i4 = plain.indexOf(keyToken("k4"));
        assertTrue(i4 > i3);
    }

    @Test
    void removalAndReAddition_movesKeyToEnd() {
        var data = new LinkedHashMap<String, Object>();
        data.put("k1", 1);
        data.put("k2", 2);
        data.put("k3", 3);

        Config cfg = manager.createConfig(TestHelper.CONFIG_TYPE, data);
        cfg.set("k2", null);
        cfg.set("k2", 2);

        String plain = cfg.toPlaintext();
        Config round = manager.createConfigFromString(TestHelper.CONFIG_TYPE, plain);

        assertEquals(List.of("k1","k3","k2"), round.getKeys(false));
        int i3 = plain.indexOf(keyToken("k3"));
        int i2 = plain.lastIndexOf(keyToken("k2"));
        assertTrue(i2 > i3);
    }

    @Test
    void randomLargeMap_roundTripsOrdering() {
        int n = 100;
        List<String> order = new ArrayList<>();
        for (int i = 0; i < n; i++) order.add("key" + i);
        Collections.shuffle(order, new Random(12345));

        var data = new LinkedHashMap<String, Object>();
        for (int i = 0; i < n; i++) data.put(order.get(i), i);

        Config cfg = manager.createConfig(TestHelper.CONFIG_TYPE, data);
        String plain = cfg.toPlaintext();
        Config round = manager.createConfigFromString(TestHelper.CONFIG_TYPE, plain);

        assertEquals(order, round.getKeys(false));
    }
}
