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

import static me.phoenixra.atumconfig.tests.helpers.TestHelper.CONFIG_TYPE;
import static org.junit.jupiter.api.Assertions.*;


/**
 * As an experiment, I mostly generated these tests by AI
 * It looks a little bit messy, but covers many cases.
 */
public class ConfigFileTest {
    @TempDir Path tmpRoot;
    private ConfigManager configManager;

    @BeforeEach
    void setUp() {
        configManager = new AtumConfigManager("test", tmpRoot, true);
    }

    // Helper to write a simple single key=>value file
    private static void writeSimpleKeyValue(Path file, String key, Object val) throws IOException {
        String content;
        switch (CONFIG_TYPE) {
            case JSON:
                content = "{\"" + key + "\":" + val + "}";
                break;
            case YAML:
                content = key + ": " + val + "\n";
                break;
            default:
                throw new IllegalStateException("Unsupported type: " + CONFIG_TYPE);
        }
        // write it out, creating or replacing the file
        Files.write(
                file,
                content.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }


    @Test
    void testLoadExistingFile() throws IOException {
        Path file = tmpRoot.resolve("test" + TestHelper.FILE_EXT);
        writeSimpleKeyValue(file, "foo", 42);

        ConfigFile cf = configManager.createConfigFile(
                CONFIG_TYPE,
                "test",
                Paths.get("test" + TestHelper.FILE_EXT),
                false
        );

        assertEquals(42, cf.getInt("foo"),
                "should read the integer value from disk");
    }

    @Test
    void testCreateNewWhenAbsent() throws IOException {
        Path file = tmpRoot.resolve("new" + TestHelper.FILE_EXT);
        assertFalse(Files.exists(file));

        ConfigFile cf = configManager.createConfigFile(
                CONFIG_TYPE,
                "new",
                Paths.get("new" + TestHelper.FILE_EXT)
        );

        assertTrue(Files.exists(file), "file should have been created");
        assertTrue(cf.getKeys(false).isEmpty(),
                "new config should start empty");
    }

    @Test
    void testReloadReflectsExternalChanges() throws IOException {
        // 1) create via API and save
        ConfigFile cf = configManager.createConfigFile(
                CONFIG_TYPE,
                "cycle",
                Paths.get("cycle" + TestHelper.FILE_EXT)
        );
        cf.set("a", 1);
        cf.save();

        // 2) overwrite *on disk* to a different value
        writeSimpleKeyValue(tmpRoot.resolve("cycle" + TestHelper.FILE_EXT), "a", 2);

        // 3) reload() should pick up that change
        cf.reload();
        assertEquals(2, cf.getInt("a"),
                "reload should reflect external edits");
    }

    @Test
    void testSaveWritesCurrentConfig() throws IOException {
        ConfigFile cf = configManager.createConfigFile(
                CONFIG_TYPE,
                "out",
                Paths.get("out" + TestHelper.FILE_EXT)
        );

        // set a top‐level and a nested value
        cf.set("x", 123);
        cf.set("nested.y", "hello");
        cf.save();

        Path outFile = tmpRoot.resolve("out" + TestHelper.FILE_EXT);
        String raw = new String(
                Files.readAllBytes(outFile),
                StandardCharsets.UTF_8
        );
        // now assert according to format
        if (CONFIG_TYPE == ConfigType.JSON) {
            assertTrue(raw.contains("\"x\": 123"),   "should serialize 'x' in JSON");
            assertTrue(raw.contains("\"nested\""),   "should include nested object key");
            assertTrue(raw.contains("\"y\": \"hello\""),
                    "should serialize nested.y in JSON");
        } else {
            // YAML
            assertTrue(raw.contains("x: 123"),       "should serialize 'x' in YAML");
            assertTrue(raw.contains("nested:"),      "should include nested object key");
            assertTrue(raw.contains("y: hello"),
                    "should serialize nested.y in YAML");
        }
    }

    @Test
    void testKeyRemovalPersists() throws IOException {
        ConfigFile cf = configManager.createConfigFile(
                CONFIG_TYPE,
                "rem",
                Paths.get("rem" + TestHelper.FILE_EXT)
        );
        cf.set("foo", 123);
        cf.save();

        cf.set("foo", null);
        cf.save();
        cf.reload();

        assertFalse(cf.hasPath("foo"),
                "setting to null should delete the key");
    }

    @Test
    void testShallowDeepGetKeys() throws IOException {
        ConfigFile cf = configManager.createConfigFile(
                CONFIG_TYPE,
                "keys",
                Paths.get("keys" + TestHelper.FILE_EXT)
        );
        cf.set("x.y.z", "v");
        cf.save();
        cf.reload();

        assertTrue(cf.getKeys(false).contains("x"),
                "shallow should see only 'x'");
        assertFalse(cf.getKeys(false).contains("x.y.z"));
        assertTrue(cf.getKeys(true).contains("x.y.z"),
                "deep should see nested key");
    }

    @Test
    void testSubsectionLiveView() throws IOException {
        ConfigFile cf = configManager.createConfigFile(
                CONFIG_TYPE,
                "sub",
                Paths.get("sub" + TestHelper.FILE_EXT)
        );
        cf.set("outer.inner", "orig");
        cf.save();

        Config sub = cf.getSubsection("outer");
        assertEquals("orig", sub.getString("inner"));

        sub.set("newKey", "added");
        cf.save();
        cf.reload();

        assertEquals("added",
                cf.getSubsection("outer").getString("newKey"),
                "mutating subsection should reflect in parent file");
    }

    @Test
    void testReloadAllAcrossFiles() throws IOException {
        ConfigFile a = configManager.createConfigFile(
                CONFIG_TYPE, "a", Paths.get("a" + TestHelper.FILE_EXT)
        );
        a.set("val", 1);
        a.save();

        ConfigFile b = configManager.createConfigFile(
                CONFIG_TYPE, "b", Paths.get("b" + TestHelper.FILE_EXT)
        );
        b.set("val", 2);
        b.save();

        // externally change only "a"
        writeSimpleKeyValue(tmpRoot.resolve("a" + TestHelper.FILE_EXT), "val", 10);

        configManager.reloadAll();
        assertEquals(10,
                configManager.getConfigFile("a").get().getInt("val"));
        assertEquals(2,
                configManager.getConfigFile("b").get().getInt("val"),
                "unmodified file should remain the same");
    }

    @Test
    void testManagerRegistryRetrieval() throws IOException {
        configManager.createConfigFile(
                CONFIG_TYPE, "one", Paths.get("one" + TestHelper.FILE_EXT)
        );
        assertTrue(configManager.getConfigFile("one").isPresent());
        assertTrue(configManager.getConfigFilesMap().containsKey("one"));
    }

    @Test
    void testForceResourceLoadFailure() {
        assertThrows(IOException.class, () ->
                configManager.createConfigFile(
                        CONFIG_TYPE,
                        "missing",
                        Paths.get("nope" + TestHelper.FILE_EXT),
                        true
                )
        );
    }

    @Test
    void testResourceFallbackSuccess() throws IOException {
        Path onDisk = tmpRoot.resolve("defaults" + TestHelper.FILE_EXT);
        Files.deleteIfExists(onDisk);

        // assumes you bundled `src/test/resources/defaults` + same FILE_EXT
        ConfigFile cf = configManager.createConfigFile(
                CONFIG_TYPE,
                "def",
                Paths.get("defaults" + TestHelper.FILE_EXT),
                false
        );

        assertTrue(Files.exists(onDisk),
                "fallback should copy bundled defaults to disk");
        assertFalse(cf.getKeys(false).isEmpty(),
                "loaded config should have at least one key");
    }
}
