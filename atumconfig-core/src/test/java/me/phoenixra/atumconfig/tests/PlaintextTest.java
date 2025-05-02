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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
            content = "{\n"
                    + "  \"first\": true,\n"
                    + "  \"second\": \"two\",\n"
                    + "  \"third\": 3\n"
                    + "}\n";
        } else {
            content = "first: true\n"
                    + "second: \"two\"\n"
                    + "third: 3\n";
        }

        Files.write(
                file,
                content.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
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
        assertEquals(Arrays.asList("alpha", "beta", "gamma"), keys);

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
                Paths.get("settings" + TestHelper.FILE_EXT),
                false
        );

        String out = file.toPlaintext();
        assertNotNull(out);

        Config round = manager.createConfigFromString(TestHelper.CONFIG_TYPE, out);
        assertTrue(round.getBool("first"));
        assertEquals("two", round.getString("second"));
        assertEquals(3,      round.getInt("third"));

        List<String> keys = round.getKeys(false);
        assertEquals(Arrays.asList("first", "second", "third"), keys);

        int i1 = out.indexOf(keyToken("first"));
        int i2 = out.indexOf(keyToken("second"));
        int i3 = out.indexOf(keyToken("third"));
        assertTrue(i1 >= 0 && i2 > i1 && i3 > i2,
                "Expected first < second < third in the serialized output");
    }

    @Test
    void nestedObjects_orderPreserved() {
        LinkedHashMap<String, Object> innerA = new LinkedHashMap<>();
        innerA.put("x", 1);
        innerA.put("y", 2);
        Map<String, Object> innerB = new LinkedHashMap<>();
        innerB.put("m", 3);
        innerB.put("n", 4);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("a", innerA);
        data.put("b", innerB);

        Config cfg = manager.createConfig(TestHelper.CONFIG_TYPE, data);
        String plain = cfg.toPlaintext();
        Config round = manager.createConfigFromString(TestHelper.CONFIG_TYPE, plain);

        assertEquals(Arrays.asList("a", "b"), round.getKeys(false));
        assertEquals(Arrays.asList("x", "y"), round.getSubsection("a").getKeys(false));
        assertEquals(Arrays.asList("m", "n"), round.getSubsection("b").getKeys(false));
    }

    @Test
    public void primitiveAndObjectLists_orderPreserved() {
        // explicit types instead of 'var'
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();

        // List.of → Arrays.asList
        data.put("primList", Arrays.asList("one", "two", "three"));

        // Map.of → Collections.singletonMap (or new HashMap<>() if you need mutability)
        Map<String, Object> o1 = Collections.singletonMap("id", "i1");
        Map<String, Object> o2 = Collections.singletonMap("id", "i2");
        data.put("objList", Arrays.asList(o1, o2));

        Config cfg   = manager.createConfig(TestHelper.CONFIG_TYPE, data);
        String plain = cfg.toPlaintext();
        Config round = manager.createConfigFromString(TestHelper.CONFIG_TYPE, plain);

        // assert on primitive list
        assertEquals(
                Arrays.asList("one", "two", "three"),
                round.getStringList("primList")
        );

        // assert on object list
        List<? extends Config> objs = round.getSubsectionList("objList");
        assertEquals(2, objs.size());
        assertEquals("i1", objs.get(0).getString("id"));
        assertEquals("i2", objs.get(1).getString("id"));
    }

    @Test
    void nullRemoval_keyExcludedInPlaintext() {
        Map<String, Object> data = new LinkedHashMap<>();
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
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("k1", 1);
        data.put("k2", 2);
        data.put("k3", 3);

        Config cfg = manager.createConfig(TestHelper.CONFIG_TYPE, data);
        cfg.set("k2", 22);

        String plain = cfg.toPlaintext();
        Config round = manager.createConfigFromString(TestHelper.CONFIG_TYPE, plain);

        assertEquals(Arrays.asList("k1","k2","k3"), round.getKeys(false));

        int i1 = plain.indexOf(keyToken("k1"));
        int i2 = plain.indexOf(keyToken("k2"));
        int i3 = plain.indexOf(keyToken("k3"));
        assertTrue(i1 >= 0 && i2 > i1 && i3 > i2);
    }

    @Test
    void appendingNewKeys_appendsAtEnd() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("k1", 1);
        data.put("k2", 2);
        data.put("k3", 3);

        Config cfg = manager.createConfig(TestHelper.CONFIG_TYPE, data);
        cfg.set("k4", 4);

        String plain = cfg.toPlaintext();
        Config round = manager.createConfigFromString(TestHelper.CONFIG_TYPE, plain);

        assertEquals(Arrays.asList("k1","k2","k3","k4"), round.getKeys(false));
        int i3 = plain.indexOf(keyToken("k3"));
        int i4 = plain.indexOf(keyToken("k4"));
        assertTrue(i4 > i3);
    }

    @Test
    void removalAndReAddition_movesKeyToEnd() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("k1", 1);
        data.put("k2", 2);
        data.put("k3", 3);

        Config cfg = manager.createConfig(TestHelper.CONFIG_TYPE, data);
        cfg.set("k2", null);
        cfg.set("k2", 2);

        String plain = cfg.toPlaintext();
        Config round = manager.createConfigFromString(TestHelper.CONFIG_TYPE, plain);

        assertEquals(Arrays.asList("k1","k3","k2"), round.getKeys(false));
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

        Map<String, Object> data = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) data.put(order.get(i), i);

        Config cfg = manager.createConfig(TestHelper.CONFIG_TYPE, data);
        String plain = cfg.toPlaintext();
        Config round = manager.createConfigFromString(TestHelper.CONFIG_TYPE, plain);

        assertEquals(order, round.getKeys(false));
    }
}
