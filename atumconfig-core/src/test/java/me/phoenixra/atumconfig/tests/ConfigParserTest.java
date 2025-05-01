package me.phoenixra.atumconfig.tests;

import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.atumconfig.api.config.ConfigType;
import me.phoenixra.atumconfig.api.config.parsers.ConfigParserExample;
import me.phoenixra.atumconfig.api.config.parsers.ExampleParseObj;
import me.phoenixra.atumconfig.core.AtumConfigManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigParserTest {
    @TempDir
    Path tmpRoot;
    private ConfigManager configManager;

    @BeforeEach
    void setUp() {
        // create the manager, pointing it at our temp directory
        configManager = new AtumConfigManager("test", tmpRoot, true);
        // register your ExampleParse parser
        configManager.addConfigParser(new ConfigParserExample());
    }

    @Test
    void testParseExistingJson() throws IOException {
        // write a little JSON file upstream of the manager
        String json = """
            {
              "id": "foo",
              "test": true,
              "value": 42
            }
            """;
        Files.writeString(tmpRoot.resolve("example.json"), json);

        // load it via your manager
        ConfigFile file = configManager.createConfigFile(
                ConfigType.JSON,
                "example",
                Path.of("example.json"),
                /*forceLoadResource=*/ false
        );

        // invoke the parser
        ExampleParseObj parsed =
                configManager
                        .getConfigParser(ExampleParseObj.class)
                        .orElseThrow()
                        .fromConfig(file);

        assertNotNull(parsed, "parser should not return null");
        assertEquals("foo", parsed.id());
        assertTrue(parsed.test());
        assertEquals(42, parsed.value());
    }

    @Test
    void testWriteThenReloadJson() throws IOException {
        // create an empty file on disk (so the manager will pick it up)
        Files.createFile(tmpRoot.resolve("example.json"));
        ConfigFile file = configManager.createConfigFile(
                ConfigType.JSON,
                "example",
                Path.of("example.json")
        );

        // build an ExampleParse and write it into the ConfigFile
        ExampleParseObj toWrite =
                new ExampleParseObj("bar", false, 7);

        configManager
                .getConfigParser(ExampleParseObj.class)
                .orElseThrow()
                .toConfig(toWrite, file);

        // save to disk
        file.save();

        // reload from disk to ensure the file really changed
        file.reload();

        // parse it back out
        ExampleParseObj parsed =
                configManager
                        .getConfigParser(ExampleParseObj.class)
                        .orElseThrow()
                        .fromConfig(file);

        assertNotNull(parsed, "parser should not return null after reload");
        assertEquals("bar", parsed.id());
        assertFalse(parsed.test());
        assertEquals(7, parsed.value());

        // and finally, check the raw JSON on disk
        String content = Files.readString(tmpRoot.resolve("example.json"));
        assertTrue(content.contains("\"id\": \"bar\""));
        assertTrue(content.contains("\"test\": false"));
        assertTrue(content.contains("\"value\": 7"));
    }



    @Test
    void getParsedOrNull_whenMissingSection_returnsNull() throws IOException {
        // empty file on disk
        Files.createFile(tmpRoot.resolve("cfg.json"));
        ConfigFile cfg = configManager.createConfigFile(
                ConfigType.JSON, "cfg", Path.of("cfg.json")
        );

        // no "doesntExist" node → getParsedOrNull should be null
        assertNull(cfg.getParsedOrNull("doesntExist", ExampleParseObj.class));
    }

    @Test
    void getParsedOrDefault_whenMissingSection_returnsDefault() throws IOException {
        Files.createFile(tmpRoot.resolve("cfg.json"));
        ConfigFile cfg = configManager.createConfigFile(
                ConfigType.JSON, "cfg", Path.of("cfg.json")
        );

        ExampleParseObj fallback = new ExampleParseObj("fallback", true, 99);
        ExampleParseObj result = cfg.getParsedOrDefault("nope", ExampleParseObj.class, fallback);

        assertSame(fallback, result, "should return exactly the default instance when missing");
    }

    @Test
    void getParsedOrNull_onNestedSection_parsesCorrectly() throws IOException {
        String json = """
            {
              "outer": {
                "id": "nestedId",
                "test": false,
                "value": 123
              }
            }
            """;
        Files.writeString(tmpRoot.resolve("nested.json"), json);

        ConfigFile cfg = configManager.createConfigFile(
                ConfigType.JSON, "nested", Path.of("nested.json"), false
        );

        ExampleParseObj parsed = cfg.getParsedOrNull("outer", ExampleParseObj.class);
        assertNotNull(parsed, "parser should return a non-null for existing nested section");
        assertEquals("nestedId", parsed.id());
        assertFalse(parsed.test());
        assertEquals(123, parsed.value());
    }

    @Test
    void getParsedOrDefault_onPresentSection_ignoresDefault() throws IOException {
        String json = """
            {
              "outer": {
                "id": "keepMe",
                "test": true,
                "value": 7
              }
            }
            """;
        Files.writeString(tmpRoot.resolve("keep.json"), json);

        ConfigFile cfg = configManager.createConfigFile(
                ConfigType.JSON, "keep", Path.of("keep.json")
        );

        ExampleParseObj fallback = new ExampleParseObj("fallback", false, -1);
        ExampleParseObj parsed = cfg.getParsedOrDefault("outer", ExampleParseObj.class, fallback);

        assertNotSame(fallback, parsed, "should not return the fallback when section exists");
        assertEquals(new ExampleParseObj("keepMe", true, 7), parsed);
    }

}
