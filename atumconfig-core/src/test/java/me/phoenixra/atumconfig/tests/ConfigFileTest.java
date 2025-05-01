package me.phoenixra.atumconfig.tests;

import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.atumconfig.api.config.ConfigType;

import me.phoenixra.atumconfig.core.AtumConfigManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigFileTest {
    @TempDir
    Path tmpRoot;
    private ConfigManager configManager;


    @BeforeEach
    void setUp() {
        configManager = new AtumConfigManager("test",tmpRoot,true);
    }

    @Test
    void testLoadExistingJson() throws IOException {
        // write a JSON file with a single key/value
        Path file = tmpRoot.resolve("test.json");
        Files.writeString(file, "{\"foo\":42}");

        // load it
        ConfigFile cf = configManager.createConfigFile(
                ConfigType.JSON, "test", Path.of("test.json"), false
        );

        // verify it read correctly
        assertEquals(42, cf.getInt("foo"), "should read the integer value from disk");
    }

    @Test
    void testCreateNewJsonWhenAbsent() throws IOException {
        // no file on disk yet
        Path file = tmpRoot.resolve("new.json");
        assertFalse(Files.exists(file));

        // creating with forceLoadResource=false should create an empty file
        ConfigFile cf = configManager.createConfigFile(
                ConfigType.JSON, "new", Path.of("new.json")
        );

        // file now exists and has no keys
        assertTrue(Files.exists(file), "new.json should have been created");
        assertTrue(cf.getKeys(false).isEmpty(), "new config should start empty");
    }

    @Test
    void testReloadReflectsExternalChanges() throws IOException {
        // first create and write a key via API
        ConfigFile cf = configManager.createConfigFile(
                ConfigType.JSON, "cycle", Path.of("cycle.json")
        );
        cf.set("a", 1);
        cf.save();

        // externally overwrite the file to a different value
        Files.writeString(tmpRoot.resolve("cycle.json"), "{\"a\":2}");

        // reload() should pick up the new disk content
        cf.reload();
        assertEquals(2, cf.getInt("a"), "reload should reflect external edits");
    }

    @Test
    void testSaveWritesCurrentConfig() throws IOException {

        ConfigFile cf = configManager.createConfigFile(
                ConfigType.JSON, "out", Path.of("out.json")
        );


        cf.set("x", 123);
        cf.set("nested.y", "hello");
        cf.save();


        String raw = Files.readString(tmpRoot.resolve("out.json"));
        assertTrue(raw.contains("\"x\": 123"), "should serialize 'x'");
        assertTrue(raw.contains("\"nested\""), "should include nested object");
        assertTrue(raw.contains("\"y\": \"hello\""), "should serialize nested.y");
    }

    @Test
    void testKeyRemovalPersists() throws IOException {
        ConfigFile cf = configManager.createConfigFile(
                ConfigType.JSON, "rem", Path.of("rem.json")
        );
        cf.set("foo", 123);
        cf.save();


        cf.set("foo", null);
        cf.save();
        cf.reload();

        assertFalse(cf.hasPath("foo"), "setting to null should delete the key");
    }

    @Test
    void testShallowDeepGetKeys() throws IOException {
        ConfigFile cf = configManager.createConfigFile(
                ConfigType.JSON, "keys", Path.of("keys.json")
        );
        cf.set("x.y.z", "v");
        cf.save();
        cf.reload();

        assertTrue( cf.getKeys(false).contains("x"),      "shallow should see only 'x'");
        assertFalse(cf.getKeys(false).contains("x.y.z"));
        assertTrue( cf.getKeys(true).contains("x.y.z"),   "deep should see nested key");
    }

    @Test
    void testSubsectionLiveView() throws IOException {
        ConfigFile cf = configManager.createConfigFile(
                ConfigType.JSON, "sub", Path.of("sub.json")
        );
        cf.set("outer.inner", "orig");
        cf.save();


        var sub = cf.getSubsection("outer");
        assertEquals("orig", sub.getString("inner"));
        sub.set("newKey", "added");
        cf.save();
        cf.reload();

        assertEquals("added", cf.getSubsection("outer").get("newKey"),
                "mutating subsection should reflect in parent file");
    }

    @Test
    void testReloadAllAcrossFiles() throws IOException {

        ConfigFile a = configManager.createConfigFile(ConfigType.JSON, "a", Path.of("a.json"));
        a.set("val", 1); a.save();
        ConfigFile b = configManager.createConfigFile(ConfigType.JSON, "b", Path.of("b.json"));
        b.set("val", 2); b.save();


        Files.writeString(tmpRoot.resolve("a.json"), "{\"val\":10}");

        configManager.reloadAll();
        assertEquals(10, configManager.getConfigFile("a").get().getInt("val"));
        assertEquals(2,  configManager.getConfigFile("b").get().getInt("val"),
                "unmodified file should remain the same");
    }

    @Test
    void testManagerRegistryRetrieval() throws IOException {
        configManager.createConfigFile(ConfigType.JSON, "one", Path.of("one.json"));
        assertTrue(configManager.getConfigFile("one").isPresent());
        assertTrue(configManager.getConfigFilesMap().containsKey("one"));
    }

    @Test
    void testForceResourceLoadFailure() {
        assertThrows(IOException.class, () ->
                configManager.createConfigFile(
                        ConfigType.JSON, "missing", Path.of("nope.json"), true
                )
        );
    }

    @Test
    void testResourceFallbackSuccess() throws IOException {

        Path resFile = tmpRoot.resolve("defaults.json");
        if (Files.exists(resFile)) Files.delete(resFile);

        // This assumes you have a bundled resource at src/test/resources/defaults.json
        ConfigFile cf = configManager.createConfigFile(
                ConfigType.JSON, "def", Path.of("defaults.json"), false
        );
        assertTrue(Files.exists(resFile), "fallback should copy bundled defaults to disk");
        assertFalse(cf.getKeys(false).isEmpty(), "loaded config should have at least one key");
    }
}
