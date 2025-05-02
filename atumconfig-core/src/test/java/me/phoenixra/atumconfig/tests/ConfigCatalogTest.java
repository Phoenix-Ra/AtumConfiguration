package me.phoenixra.atumconfig.tests;

import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.atumconfig.api.config.catalog.ConfigCatalog;
import me.phoenixra.atumconfig.api.config.catalog.ConfigCatalogListener;
import me.phoenixra.atumconfig.core.AtumConfigManager;
import me.phoenixra.atumconfig.tests.helpers.TestHelper;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;


import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * As an experiment, I mostly generated these tests by AI
 * It looks a little bit messy, but covers many cases.
 */
class ConfigCatalogTest {
    @TempDir Path tmpRoot;
    private ConfigManager configManager;

    @BeforeEach
    void setUp() {
        configManager = new AtumConfigManager("test", tmpRoot, true);
    }

    //───────────────────────────────────────────────────────────
    // 1) INITIAL LOAD SCENARIOS
    //───────────────────────────────────────────────────────────

    record Scenario(
            String name,
            boolean nested,
            boolean fromDisk,
            Consumer<Path> setupFs,
            List<String> expectedIds,
            boolean expectLoadDefaults
    ) {
        @Override public String toString() { return name; }
    }

    static Stream<Arguments> initialLoadScenarios() {
        return Stream.of(
                Arguments.of(new Scenario(
                        "Disk / Flat",
                        false, true,
                        ConfigCatalogTest::setupDiskFlat,
                        List.of("good1", "good2"),
                        false
                )),
                Arguments.of(new Scenario(
                        "Disk / Nested",
                        true, true,
                        ConfigCatalogTest::setupDiskNested,
                        List.of("good1", "good2", "pups/good0"),
                        false
                )),
                Arguments.of(new Scenario(
                        "Resource / Flat",
                        false, false,
                        base -> { /* nothing on disk; load from src/test/resources/catalog/* + FILE_EXT */ },
                        Collections.emptyList(),
                        true
                )),
                Arguments.of(new Scenario(
                        "Resource / Nested",
                        true, false,
                        base -> { /* nothing on disk */ },
                        Collections.emptyList(),
                        true
                ))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("initialLoadScenarios")
    void testInitialLoad(Scenario sc) throws IOException {
        Path base = tmpRoot.resolve("catalog");
        if (sc.fromDisk) Files.createDirectories(base);
        sc.setupFs.accept(base);

        List<String> loaded = new ArrayList<>();
        AtomicBoolean defaults = new AtomicBoolean(false);

        ConfigCatalog catalog = configManager.createCatalog(
                TestHelper.CONFIG_TYPE,
                "cat",
                Path.of("catalog"),
                sc.nested,
                new ConfigCatalogListener() {
                    public void onClear()            { loaded.clear(); defaults.set(false); }
                    public void onConfigLoaded(@NotNull ConfigFile cf) {
                        loaded.add(cf.getId());
                    }
                    public void afterLoadDefaults()     { defaults.set(true); }
                }
        );

        catalog.reload();

        if (sc.fromDisk) {
            assertFalse(defaults.get(), sc.name + ": should _not_ load defaults");
            assertEquals(sc.expectedIds.size(), loaded.size(), sc.name);
            assertTrue(loaded.containsAll(sc.expectedIds), sc.name);
            assertEquals(sc.expectedIds.size(),
                    catalog.getConfigFilesMap().size(),
                    sc.name + ": map size"
            );
        } else {
            assertTrue(defaults.get(), sc.name + ": should load defaults");
            assertFalse(loaded.isEmpty(), sc.name + ": must fire listener");
            assertEquals(loaded.size(),
                    catalog.getConfigFilesMap().size(),
                    sc.name + ": map size"
            );
        }
    }

    private static void setupDiskFlat(Path base) {
        try {
            Files.createFile(base.resolve("good1" + TestHelper.FILE_EXT));
            Files.createFile(base.resolve("good2" + TestHelper.FILE_EXT));
            Files.createFile(base.resolve("fail1" + TestHelper.BAD_FILE_EXT));
            Files.createFile(base.resolve("fail2.toml"));
            Files.createFile(base.resolve("ignore.abracadabra"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void setupDiskNested(Path base) {
        try {
            Files.createFile(base.resolve("good1" + TestHelper.FILE_EXT));
            Files.createFile(base.resolve("good2" + TestHelper.FILE_EXT));
            Files.createFile(base.resolve("fail1" + TestHelper.BAD_FILE_EXT));
            Path pups = base.resolve("pups");
            Files.createDirectories(pups);
            Files.createFile(pups.resolve("good0" + TestHelper.FILE_EXT));
            Files.createFile(pups.resolve("oops.txt"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    //───────────────────────────────────────────────────────────
    // 2) INCREMENTAL RELOAD
    //───────────────────────────────────────────────────────────

    @Test
    void testIncrementalReload() throws IOException {
        Path base = tmpRoot.resolve("catalog");
        Files.createDirectories(base);
        Files.createFile(base.resolve("a" + TestHelper.FILE_EXT));
        Files.createFile(base.resolve("b" + TestHelper.FILE_EXT));

        List<String> loaded = new ArrayList<>();
        ConfigCatalog c = configManager.createCatalog(
                TestHelper.CONFIG_TYPE, "cat", Path.of("catalog"), false,
                new SimpleListener(loaded, null)
        );
        c.reload();
        assertTrue(loaded.containsAll(List.of("a", "b")));

        // add c, remove b
        Files.createFile(base.resolve("c" + TestHelper.FILE_EXT));
        Files.delete(base.resolve("b" + TestHelper.FILE_EXT));

        loaded.clear();
        c.reload();
        assertTrue(loaded.contains("a"), "a still there");
        assertTrue(loaded.contains("c"), "new c loaded");
        assertFalse(loaded.contains("b"), "b removed");
        assertEquals(2, loaded.size());
    }

    //───────────────────────────────────────────────────────────
    // 3) MODIFY-ON-DISK & reload
    //───────────────────────────────────────────────────────────

    @Test
    void testModifyOnDiskReload() throws IOException {
        Path base = tmpRoot.resolve("catalog");
        Files.createDirectories(base);
        Path f = base.resolve("m" + TestHelper.FILE_EXT);

        // write x=1 in the correct format
        writeSimpleKeyValue(f, "x", 1);

        ConfigCatalog c = configManager.createCatalog(
                TestHelper.CONFIG_TYPE, "cat", Path.of("catalog"), false,
                new SimpleListener(null, null)
        );
        c.reload();
        ConfigFile cf = c.getConfigFile("m").get();
        assertEquals(1, cf.getInt("x"));

        // overwrite to x=99
        writeSimpleKeyValue(f, "x", 99);
        cf.reload();
        assertEquals(99, cf.getInt("x"));
    }

    //───────────────────────────────────────────────────────────
    // 4) Listener Ordering
    //───────────────────────────────────────────────────────────

    @Test
    void testListenerOrdering() {
        List<String> events = new ArrayList<>();
        ConfigCatalog c = configManager.createCatalog(
                TestHelper.CONFIG_TYPE, "cat", Path.of("catalog"), false,
                new ConfigCatalogListener() {
                    public void onClear()            { events.add("clear"); }
                    public void onConfigLoaded(@NotNull ConfigFile cf) { events.add("load:" + cf.getId()); }
                    public void afterLoadDefaults()     { events.add("defaults"); }
                }
        );

        // no files → resource fallback
        c.reload();
        assertEquals("clear", events.get(0), "clear first");
        assertTrue(events.stream().skip(1)
                .allMatch(e -> e.startsWith("defaults") || e.startsWith("load:")));
    }

    //───────────────────────────────────────────────────────────
    // 5) ERROR HANDLING: bad file formats
    //───────────────────────────────────────────────────────────

    @Test
    void testErrorHandlingBadFile() throws IOException {
        Path base = tmpRoot.resolve("catalog");
        Files.createDirectories(base);

        // good file
        writeSimpleKeyValue(base.resolve("good" + TestHelper.FILE_EXT), "ok", 1);
        // bad file
        writeInvalidFile(base.resolve("bad" + TestHelper.FILE_EXT));

        List<String> loaded = new ArrayList<>();
        ConfigCatalog c = configManager.createCatalog(
                TestHelper.CONFIG_TYPE, "cat", Path.of("catalog"), false,
                new SimpleListener(loaded, null)
        );

        c.reload();
        assertTrue(loaded.contains("good"));
        assertFalse(loaded.contains("bad"));
    }

    //───────────────────────────────────────────────────────────
    // 7) CONCURRENT CATALOGS
    //───────────────────────────────────────────────────────────

    @Test
    void testConcurrentCatalogsIsolation() throws IOException {
        Path aDir = tmpRoot.resolve("a");
        Files.createDirectories(aDir);
        Files.createFile(aDir.resolve("one" + TestHelper.FILE_EXT));

        Path bDir = tmpRoot.resolve("b");
        Files.createDirectories(bDir);
        Files.createFile(bDir.resolve("two" + TestHelper.FILE_EXT));

        List<String> la = new ArrayList<>();
        ConfigCatalog ca = configManager.createCatalog(
                TestHelper.CONFIG_TYPE, "A", Path.of("a"), false,
                new SimpleListener(la, null)
        );

        List<String> lb = new ArrayList<>();
        ConfigCatalog cb = configManager.createCatalog(
                TestHelper.CONFIG_TYPE, "B", Path.of("b"), false,
                new SimpleListener(lb, null)
        );

        ca.reload();
        cb.reload();

        assertTrue(la.contains("one"));
        assertFalse(la.contains("two"));
        assertTrue(lb.contains("two"));
        assertFalse(lb.contains("one"));
    }

    //───────────────────────────────────────────────────────────
    // 8) PERFORMANCE SMOKE TEST
    //───────────────────────────────────────────────────────────

    @Test
    void testPerformanceLargeScale() throws IOException {
        Path base = tmpRoot.resolve("catalog");
        Files.createDirectories(base);
        for (int i = 0; i < 5_000; i++) {
            writeSimpleKeyValue(
                    base.resolve("f" + i + TestHelper.FILE_EXT),
                    "i", i
            );
        }

        AtomicBoolean fired = new AtomicBoolean(false);
        ConfigCatalog c = configManager.createCatalog(
                TestHelper.CONFIG_TYPE, "cat", Path.of("catalog"), false,
                new SimpleListener(null, fired)
        );

        long start = System.currentTimeMillis();
        c.reload();
        long elapsed = System.currentTimeMillis() - start;

        assertTrue(elapsed < 2_000, "reload 5k should be <2s");
        assertTrue(fired.get(), "listener should fire");
        assertEquals(5_000, c.getConfigFilesMap().size());
    }

    //───────────────────────────────────────────────────────────
    //  Helpers & Listener
    //───────────────────────────────────────────────────────────

    private static void writeSimpleKeyValue(Path f, String key, int val) throws IOException {
        String content;
        switch (TestHelper.CONFIG_TYPE) {
            case JSON  -> content = "{\"" + key + "\":" + val + "}";
            case YAML  -> content = key + ": " + val + "\n";
            default     -> throw new IllegalStateException("unsupported: " + TestHelper.CONFIG_TYPE);
        }
        Files.writeString(f, content);
    }

    private static void writeInvalidFile(Path f) throws IOException {
        String invalid;
        switch (TestHelper.CONFIG_TYPE) {
            case JSON  -> invalid = "{ not valid JSON }";
            case YAML  -> invalid = "not valid: [unbalanced";
            default     -> throw new IllegalStateException("unsupported: " + TestHelper.CONFIG_TYPE);
        }
        Files.writeString(f, invalid);
    }

    private static class SimpleListener implements ConfigCatalogListener {
        private final List<String> out;
        private final AtomicBoolean flag;
        SimpleListener(List<String> out, AtomicBoolean flag) {
            this.out = out; this.flag = flag;
        }
        @Override public void onClear() { if (out != null) out.clear(); }
        @Override public void onConfigLoaded(@NotNull ConfigFile cf) {
            if (flag != null) flag.set(true);
            if (out  != null) out.add(cf.getId());
        }
        @Override public void afterLoadDefaults() { /* no-op */ }
    }
}