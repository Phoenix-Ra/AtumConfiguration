package me.phoenixra.atumconfig.tests;

import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.atumconfig.api.config.ConfigType;
import me.phoenixra.atumconfig.api.config.catalog.ConfigCatalog;
import me.phoenixra.atumconfig.api.config.catalog.ConfigCatalogListener;
import me.phoenixra.atumconfig.core.AtumConfigManager;
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


class ConfigCatalogTest {
    @TempDir Path tmpRoot;
    private ConfigManager configManager;


    @BeforeEach
    void setUp() {
        configManager = new AtumConfigManager("test",tmpRoot,true);
    }



    //───────────────────────────────────────────────────────────────────────────
    // 1) INITIAL LOAD SCENARIOS: disk vs. resource, flat vs. nested
    //───────────────────────────────────────────────────────────────────────────

    record Scenario(
            String name,
            boolean nested,
            boolean fromDisk,
            Consumer<Path> setupFs,
            List<String> expectedIds,
            boolean expectLoadDefaults
    ) {
        @Override
        public String toString() {
            return name;
        }
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
                        base -> {
                            // no on-disk files; expect bundled at src/test/resources/catalog/*.json
                        },
                        Collections.emptyList(),  // ignored for resource
                        true
                )),
                Arguments.of(new Scenario(
                        "Resource / Nested",
                        true, false,
                        base -> { /* no on-disk */ },
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

        List<String> accepted = new ArrayList<>();
        AtomicBoolean loadedDefaults = new AtomicBoolean(false);

        ConfigCatalog catalog = configManager.createCatalog(
                ConfigType.JSON,
                "cat",
                Path.of("catalog"),
                sc.nested,
                new ConfigCatalogListener() {
                    public void onClear() { accepted.clear(); loadedDefaults.set(false); }
                    public void onConfigLoaded(ConfigFile cf) { accepted.add(cf.getId()); }
                    public void onLoadDefaults() { loadedDefaults.set(true); }
                }
        );

        catalog.reload();

        if (sc.fromDisk) {
            assertFalse(loadedDefaults.get(), sc.name + ": should not load defaults");
            assertEquals(sc.expectedIds.size(), accepted.size(), sc.name);
            assertTrue(accepted.containsAll(sc.expectedIds), sc.name);
            // also verify catalog map
            assertEquals(sc.expectedIds.size(),
                    catalog.getConfigFilesMap().size(),
                    sc.name + ": map size");
        } else {
            assertTrue(loadedDefaults.get(), sc.name + ": should have loaded defaults");
            assertFalse(accepted.isEmpty(),  sc.name + ": listener must fire");
            assertEquals(accepted.size(),
                    catalog.getConfigFilesMap().size(),
                    sc.name + ": map size");
        }
    }

    private static void setupDiskFlat(Path base) {
        try {
            Files.createFile(base.resolve("good1.json"));
            Files.createFile(base.resolve("good2.json"));
            Files.createFile(base.resolve("fail1.yml"));
            Files.createFile(base.resolve("fail2.toml"));
            Files.createFile(base.resolve("ignore.abracadabra"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void setupDiskNested(Path base) {
        try {
            // root
            Files.createFile(base.resolve("good1.json"));
            Files.createFile(base.resolve("good2.json"));
            Files.createFile(base.resolve("fail1.yml"));
            // nested
            Path pups = base.resolve("pups");
            Files.createDirectories(pups);
            Files.createFile(pups.resolve("good0.json"));
            Files.createFile(pups.resolve("oops.txt"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    //───────────────────────────────────────────────────────────────────────────
    // 2) INCREMENTAL RELOAD: add & remove files
    //───────────────────────────────────────────────────────────────────────────

    @Test
    void testIncrementalReload() throws IOException {
        Path base = tmpRoot.resolve("catalog");
        Files.createDirectories(base);
        Files.createFile(base.resolve("a.json"));
        Files.createFile(base.resolve("b.json"));

        List<String> loaded = new ArrayList<>();
        ConfigCatalog c = configManager.createCatalog(
                ConfigType.JSON, "cat", Path.of("catalog"), false,
                new SimpleListener(loaded, null)
        );
        c.reload();
        assertTrue(loaded.containsAll(List.of("a","b")));

        // now add c.json, remove b.json
        Files.createFile(base.resolve("c.json"));
        Files.delete(base.resolve("b.json"));

        loaded.clear();
        c.reload();
        assertTrue(loaded.contains("a"), "a still there");
        assertTrue(loaded.contains("c"), "new c loaded");
        assertFalse(loaded.contains("b"), "b removed");
        assertEquals(2, loaded.size());
    }

    //───────────────────────────────────────────────────────────────────────────
    // 3) MODIFY‐ON‐DISK & reload reflects new contents
    //───────────────────────────────────────────────────────────────────────────

    @Test
    void testModifyOnDiskReload() throws IOException {
        Path base = tmpRoot.resolve("catalog");
        Files.createDirectories(base);
        Path f = base.resolve("m.json");
        Files.writeString(f, "{\"x\":1}");

        ConfigCatalog c = configManager.createCatalog(
                ConfigType.JSON, "cat", Path.of("catalog"), false,
                new SimpleListener(null, null)
        );
        c.reload();
        ConfigFile cf = c.getConfigFile("m").get();
        assertEquals(1, cf.getInt("x"));

        // overwrite file on disk
        Files.writeString(f, "{\"x\":99}");
        cf.reload();
        assertEquals(99, cf.getInt("x"));
    }

    //───────────────────────────────────────────────────────────────────────────
    // 4) onClear() happens before onConfigLoaded()
    //───────────────────────────────────────────────────────────────────────────

    @Test
    void testListenerOrdering() {
        List<String> events = new ArrayList<>();
        ConfigCatalog c = configManager.createCatalog(
                ConfigType.JSON, "cat", Path.of("catalog"), false,
                new ConfigCatalogListener() {
                    public void onClear() { events.add("clear"); }
                    public void onConfigLoaded(ConfigFile cf) { events.add("load:" + cf.getId()); }
                    public void onLoadDefaults() { events.add("defaults"); }
                }
        );

        // no files on disk → resource fallback
        c.reload();
        assertEquals("clear", events.get(0), "clear first");
        // after clear, either defaults or load events
        assertTrue(events.stream().skip(1).allMatch(e -> e.startsWith("defaults") || e.startsWith("load:")));
    }

    //───────────────────────────────────────────────────────────────────────────
    // 5) ERROR HANDLING: bad file formats
    //───────────────────────────────────────────────────────────────────────────

    @Test
    void testErrorHandlingBadFile() throws IOException {
        Path base = tmpRoot.resolve("catalog");
        Files.createDirectories(base);
        Files.writeString(base.resolve("good.json"), "{\"ok\":true}");
        Files.writeString(base.resolve("bad.json"), "{ not valid JSON }");

        List<String> loaded = new ArrayList<>();
        ConfigCatalog c = configManager.createCatalog(
                ConfigType.JSON, "cat", Path.of("catalog"), false,
                new SimpleListener(loaded, null)
        );

        c.reload();
        assertTrue(loaded.contains("good"));
        assertFalse(loaded.contains("bad"));
    }


    //───────────────────────────────────────────────────────────────────────────
    // 7) Concurrent Catalogs Isolation
    //───────────────────────────────────────────────────────────────────────────

    @Test
    void testConcurrentCatalogsIsolation() throws IOException {
        Path aDir = tmpRoot.resolve("a");
        Files.createDirectories(aDir);
        Files.createFile(aDir.resolve("one.json"));

        Path bDir = tmpRoot.resolve("b");
        Files.createDirectories(bDir);
        Files.createFile(bDir.resolve("two.json"));

        List<String> la = new ArrayList<>();
        ConfigCatalog ca = configManager.createCatalog(
                ConfigType.JSON, "A", Path.of("a"), false,
                new SimpleListener(la, null)
        );

        List<String> lb = new ArrayList<>();
        ConfigCatalog cb = configManager.createCatalog(
                ConfigType.JSON, "B", Path.of("b"), false,
                new SimpleListener(lb, null)
        );

        ca.reload();
        cb.reload();

        assertTrue(la.contains("one"));
        assertFalse(la.contains("two"));
        assertTrue(lb.contains("two"));
        assertFalse(lb.contains("one"));
    }

    //───────────────────────────────────────────────────────────────────────────
    // 8) Performance Smoke Test (optional / @Disabled by default)
    //───────────────────────────────────────────────────────────────────────────

    @Test
    void testPerformanceLargeScale() throws IOException {
        Path base = tmpRoot.resolve("catalog");
        Files.createDirectories(base);
        for (int i = 0; i < 5_000; i++) {
            Files.writeString(base.resolve("f" + i + ".json"), "{\"i\": " + i + "}");
        }

        AtomicBoolean fired = new AtomicBoolean(false);
        ConfigCatalog c = configManager.createCatalog(
                ConfigType.JSON, "cat", Path.of("catalog"), false,
                new SimpleListener(null, fired)
        );

        long start = System.currentTimeMillis();
        c.reload();
        long elapsed = System.currentTimeMillis() - start;

        assertTrue(elapsed < 2_000, "reload 5k files should be <2s");
        assertTrue(fired.get(), "listener should have fired");
        assertEquals(5_000, c.getConfigFilesMap().size());
    }

    //───────────────────────────────────────────────────────────────────────────
    //  Support: simple listener implementation
    //───────────────────────────────────────────────────────────────────────────

    private static class SimpleListener implements ConfigCatalogListener {
        private final List<String> out;
        private final AtomicBoolean configAccepted;

        SimpleListener(List<String> out, AtomicBoolean configAccepted) {
            this.out = out;
            this.configAccepted = configAccepted;
        }

        @Override public void onClear() { if (out != null) out.clear(); }
        @Override public void onConfigLoaded(ConfigFile cf) {
            if (configAccepted != null) configAccepted.set(true);
            if (out != null) out.add(cf.getId());
        }
        @Override public void onLoadDefaults() {

        }
    }
}