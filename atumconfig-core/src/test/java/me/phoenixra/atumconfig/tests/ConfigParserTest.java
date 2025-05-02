package me.phoenixra.atumconfig.tests;

import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumconfig.api.config.ConfigFile;

import me.phoenixra.atumconfig.api.config.ConfigType;
import me.phoenixra.atumconfig.api.config.parsers.ConfigParserExample;
import me.phoenixra.atumconfig.api.config.parsers.ExampleParseObj;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * As an experiment, I mostly generated these tests by AI
 * It looks a little bit messy, but covers many cases.
 */
public class ConfigParserTest {
    @TempDir
    Path tmpRoot;
    private ConfigManager manager;

    @BeforeEach
    void setUp() {
        manager = new AtumConfigManager("test", tmpRoot, true);
        manager.addConfigParser(new ConfigParserExample());
    }
    private static boolean isJson() {
        return TestHelper.CONFIG_TYPE == ConfigType.JSON;
    }
    private void writeTestObj(Path file, String id, boolean test, int value) throws IOException {
        String content;
        switch (TestHelper.CONFIG_TYPE) {
            case JSON:
                content = String.format(
                        "{ \"id\": \"%s\", \"test\": %s, \"value\": %d }",
                        id, test, value
                );
                break;
            case YAML:
                content = String.format(
                        "id: %s%n" +
                                "test: %s%n" +
                                "value: %d%n",
                        id, test, value
                );
                break;
            default:
                throw new IllegalStateException("Unsupported: " + TestHelper.CONFIG_TYPE);
        }
        Files.write(
                file,
                content.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    // helper: write a nested under 'outer'
    private void writeNestedTestObj(Path file, String id, boolean test, int value) throws IOException {
        String content;
        switch (TestHelper.CONFIG_TYPE) {
            case JSON:
                content = String.format(
                        "{ \"outer\": { \"id\": \"%s\", \"test\": %s, \"value\": %d } }",
                        id, test, value
                );
                break;
            case YAML:
                content = String.format(
                        "outer:%n" +
                                "  id: %s%n" +
                                "  test: %s%n" +
                                "  value: %d%n",
                        id, test, value
                );
                break;
            default:
                throw new IllegalStateException("Unsupported: " + TestHelper.CONFIG_TYPE);
        }
        Files.write(
                file,
                content.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    @Test
    void testParseExisting() throws IOException {
        Path f = tmpRoot.resolve("example" + TestHelper.FILE_EXT);
        writeTestObj(f, "foo", true, 42);

        ConfigFile file = manager.createConfigFile(
                TestHelper.CONFIG_TYPE,
                "example",
                Paths.get("example" + TestHelper.FILE_EXT),
                false
        );

        ExampleParseObj parsed = manager
                .getConfigParser(ExampleParseObj.class)
                .orElseThrow(RuntimeException::new)
                .fromConfig(file);

        assertNotNull(parsed, "parser should not return null");
        assertEquals("foo", parsed.getId());
        assertTrue(parsed.isTest());
        assertEquals(42, parsed.getValue());
    }

    @Test
    void testWriteThenReload() throws IOException {
        Path f = tmpRoot.resolve("example" + TestHelper.FILE_EXT);
        Files.createFile(f);

        ConfigFile file = manager.createConfigFile(
                TestHelper.CONFIG_TYPE,
                "example",
                Paths.get("example" + TestHelper.FILE_EXT)
        );

        // write via parser + save + reload
        ExampleParseObj toWrite = new ExampleParseObj("bar", false, 7);
        manager.getConfigParser(ExampleParseObj.class).orElseThrow(RuntimeException::new)
                .toConfig(toWrite, file);
        file.save();
        file.reload();

        ExampleParseObj parsed = manager
                .getConfigParser(ExampleParseObj.class).orElseThrow(RuntimeException::new)
                .fromConfig(file);

        assertNotNull(parsed, "parser should not return null after reload");
        assertEquals("bar", parsed.getId());
        assertFalse(parsed.isTest());
        assertEquals(7, parsed.getValue());

        // raw‐file assertions differ by format:
        Path outFile = tmpRoot.resolve(f);
        String raw = new String(
                Files.readAllBytes(outFile),
                StandardCharsets.UTF_8
        );
        if (TestHelper.CONFIG_TYPE == ConfigType.JSON) {
            assertTrue(raw.contains("\"id\": \"bar\""));
            assertTrue(raw.contains("\"test\": false"));
            assertTrue(raw.contains("\"value\": 7"));
        } else {
            // YAML
            assertTrue(raw.contains("id: bar"));
            assertTrue(raw.contains("test: false"));
            assertTrue(raw.contains("value: 7"));
        }
    }

    @Test
    void getParsedOrNull_whenMissingSection_returnsNull() throws IOException {
        Path f = tmpRoot.resolve("cfg" + TestHelper.FILE_EXT);
        Files.createFile(f);
        ConfigFile cfg = manager.createConfigFile(
                TestHelper.CONFIG_TYPE, "cfg", Paths.get("cfg" + TestHelper.FILE_EXT)
        );
        assertNull(cfg.getParsedOrNull("doesntExist", ExampleParseObj.class));
    }

    @Test
    void getParsedOrDefault_whenMissingSection_returnsDefault() throws IOException {
        Path f = tmpRoot.resolve("cfg" + TestHelper.FILE_EXT);
        Files.createFile(f);
        ConfigFile cfg = manager.createConfigFile(
                TestHelper.CONFIG_TYPE, "cfg", Paths.get("cfg" + TestHelper.FILE_EXT)
        );

        ExampleParseObj fallback = new ExampleParseObj("fallback", true, 99);
        ExampleParseObj result = cfg.getParsedOrDefault("nope", ExampleParseObj.class, fallback);

        assertSame(fallback, result,
                "should return exactly the default instance when missing");
    }

    @Test
    void getParsedOrNull_onNestedSection_parsesCorrectly() throws IOException {
        Path f = tmpRoot.resolve("nested" + TestHelper.FILE_EXT);
        writeNestedTestObj(f, "nestedId", false, 123);

        ConfigFile cfg = manager.createConfigFile(
                TestHelper.CONFIG_TYPE, "nested", Paths.get("nested" + TestHelper.FILE_EXT), false
        );

        ExampleParseObj parsed = cfg.getParsedOrNull("outer", ExampleParseObj.class);
        assertNotNull(parsed,
                "parser should return a non-null for existing nested section");
        assertEquals("nestedId", parsed.getId());
        assertFalse(parsed.isTest());
        assertEquals(123, parsed.getValue());
    }

    @Test
    void getParsedOrDefault_onPresentSection_ignoresDefault() throws IOException {
        Path f = tmpRoot.resolve("keep" + TestHelper.FILE_EXT);
        writeNestedTestObj(f, "keepMe", true, 7);

        ConfigFile cfg = manager.createConfigFile(
                TestHelper.CONFIG_TYPE, "keep", Paths.get("keep" + TestHelper.FILE_EXT)
        );

        ExampleParseObj fallback = new ExampleParseObj("fallback", false, -1);
        ExampleParseObj parsed = cfg.getParsedOrDefault("outer", ExampleParseObj.class, fallback);

        assertNotSame(fallback, parsed,
                "should not return the fallback when section exists");
        assertEquals(new ExampleParseObj("keepMe", true, 7), parsed);
    }



    @Test
    void getParsedListOrNull_missingPath_returnsNull() {
        manager.addConfigParser(new ConfigParserExample());
        String raw = isJson() ? "{ }" : "";
        Config cfg = manager.createConfigFromString(TestHelper.CONFIG_TYPE, raw);
        assertNull(cfg.getParsedListOrNull("items", ExampleParseObj.class));
    }

    @Test
    void getParsedListOrNull_notList_returnsNull() {
        manager.addConfigParser(new ConfigParserExample());
        String raw = isJson()
                ? "{ \"items\": { \"id\":\"a\",\"test\":true,\"value\":1 } }"
                : "items:\n" +
                "  id: a\n" +
                "  test: true\n" +
                "  value: 1\n";
        Config cfg = manager.createConfigFromString(TestHelper.CONFIG_TYPE, raw);
        assertNull(cfg.getParsedListOrNull("items", ExampleParseObj.class));
    }

    @Test
    void getParsedListOrNull_returnsParsedList() {
        manager.addConfigParser(new ConfigParserExample());
        String raw = isJson()
                ? "{ \"items\": ["
                + "{\"id\":\"a\",\"test\":true,\"value\":1},"
                + "{\"id\":\"b\",\"test\":false,\"value\":2}"
                + "] }"
                : "items:\n" +
                "  - id: a\n" +
                "    test: true\n" +
                "    value: 1\n" +
                "  - id: b\n" +
                "    test: false\n" +
                "    value: 2\n";
        Config cfg = manager.createConfigFromString(TestHelper.CONFIG_TYPE, raw);

        List<ExampleParseObj> list = cfg.getParsedListOrNull("items", ExampleParseObj.class);
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals(new ExampleParseObj("a", true, 1),  list.get(0));
        assertEquals(new ExampleParseObj("b", false, 2), list.get(1));
    }

    @Test
    void getParsedListOrNull_filtersInvalidElements() {
        manager.addConfigParser(new ConfigParserExample());
        String raw = isJson()
                ? "{ \"items\": ["
                + "{\"id\":\"a\",\"test\":true,\"value\":1},"
                + "{\"id\":\"b\",\"test\":false}"    // missing value
                + "] }"
                : "items:\n" +
                "  - id: a\n" +
                "    test: true\n" +
                "    value: 1\n" +
                "  - id: b\n" +
                "    test: false\n";               // missing value
        Config cfg = manager.createConfigFromString(TestHelper.CONFIG_TYPE, raw);

        List<ExampleParseObj> list = cfg.getParsedListOrNull("items", ExampleParseObj.class);
        assertNotNull(list);
        assertEquals(1, list.size(), "invalid entries should be filtered out");
        assertEquals(new ExampleParseObj("a", true, 1), list.get(0));
    }
}
