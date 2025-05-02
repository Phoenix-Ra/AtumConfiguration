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
public class SubsectionsTest {
    @TempDir Path tmpRoot;
    private ConfigManager manager;

    @BeforeEach
    void setUp() {
        manager = new AtumConfigManager("test", tmpRoot, true);
    }

    private boolean isJson() {
        return TestHelper.CONFIG_TYPE == ConfigType.JSON;
    }

    /**
     * Produce a two-key mapping: one nested object under `outer` and
     * one top-level primitive `leaf`.  Works for JSON or YAML.
     */
    private String mkNested(String outer, String innerKey, Object innerVal,
                            String leafKey, Object leafVal) {
        if (isJson()) {
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"").append(outer).append("\":{")
                    .append("\"").append(innerKey).append("\":")
                    .append(innerVal instanceof String
                            ? "\"" + innerVal + "\""
                            : innerVal)
                    .append("},")
                    .append("\"").append(leafKey).append("\":")
                    .append(leafVal instanceof String
                            ? "\"" + leafVal + "\""
                            : leafVal)
                    .append("}");
            return sb.toString();
        } else {
            // YAML indentation
            return outer + ":\n" +
                    "  " + innerKey + ": " + innerVal + "\n" +
                    leafKey + ": " + leafVal + "\n";
        }
    }

    @Test
    void testGetAllSubsections_emptyInMemory() {
        Config cfg = manager.createConfig(TestHelper.CONFIG_TYPE, null);
        Map<String, Config> subs = cfg.getAllSubsections();
        assertTrue(subs.isEmpty(), "No subsections in a fresh config");
    }

    @Test
    void testGetAllSubsections_inMemoryWithSubsections() {
        Config cfg = manager.createConfig(TestHelper.CONFIG_TYPE, null);
        cfg.set("a.x", 1);
        cfg.set("b.y", 2);
        cfg.set("c", "primitive");  // not a subsection

        Map<String, Config> subs = cfg.getAllSubsections();
        // only "a" and "b" should appear, in that insertion order
        assertEquals(List.of("a", "b"), new ArrayList<>(subs.keySet()));
        assertEquals(1, subs.get("a").getInt("x"));
        assertEquals(2, subs.get("b").getInt("y"));
        assertFalse(subs.containsKey("c"), "primitive key 'c' shouldn't appear");
    }

    @Test
    void testGetAllSubsections_fileBacked() throws IOException {
        // write a file with one nested object and one primitive leaf
        Path f = tmpRoot.resolve("data" + TestHelper.FILE_EXT);
        Files.writeString(f, mkNested("outer", "i", 1, "leaf", "val"));

        ConfigFile cf = manager.createConfigFile(
                TestHelper.CONFIG_TYPE,
                "data",
                Path.of("data" + TestHelper.FILE_EXT),
                false
        );

        Map<String, Config> subs = cf.getAllSubsections();
        // should only contain the "outer" section
        assertEquals(List.of("outer"), new ArrayList<>(subs.keySet()));
        assertFalse(subs.containsKey("leaf"), "'leaf' is not a subsection");

        Config outer = subs.get("outer");
        assertNotNull(outer, "outer should map to a valid Config");
        assertEquals(1, outer.getInt("i"), "nested 'i' should round-trip");
    }
}