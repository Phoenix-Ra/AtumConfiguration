package me.phoenixra.atumconfig.tests;

import me.phoenixra.atumconfig.api.ConfigLogger;
import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumconfig.api.config.ConfigFile;

import me.phoenixra.atumconfig.api.config.ConfigType;
import me.phoenixra.atumconfig.api.placeholders.Placeholder;
import me.phoenixra.atumconfig.api.placeholders.PlaceholderHandler;
import me.phoenixra.atumconfig.api.placeholders.context.PlaceholderContext;
import me.phoenixra.atumconfig.api.placeholders.context.PlaceholderList;
import me.phoenixra.atumconfig.api.placeholders.types.DynamicPlaceholder;
import me.phoenixra.atumconfig.api.placeholders.types.StaticPlaceholder;
import me.phoenixra.atumconfig.core.AtumConfigManager;
import me.phoenixra.atumconfig.core.AtumPlaceholderHandler;
import me.phoenixra.atumconfig.tests.helpers.TestHelper;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * As an experiment, I mostly generated these tests by AI
 * It looks a little bit messy, but covers many cases.
 */
public class PlaceholdersTest {

    private AtumPlaceholderHandler handler;

    @TempDir Path tmpRoot;
    private ConfigManager manager;

    @BeforeEach
    void setUp() {
        handler = new AtumPlaceholderHandler(ConfigLogger.EMPTY);
        manager = new AtumConfigManager("test", tmpRoot, true);
        manager.setPlaceholderHandler(handler);
    }

    @Test
    void testFindPlaceholdersIn_emptyAndNonMatching() {
        assertTrue(PlaceholderHandler.findPlaceholdersIn("no placeholders here").isEmpty());
        assertTrue(PlaceholderHandler.findPlaceholdersIn("%%not a placeholder%%").isEmpty());
    }

    @Test
    void testFindPlaceholdersIn_multiple() {
        String text = "Hello %foo% and %bar%!";
        List<String> found = PlaceholderHandler.findPlaceholdersIn(text);
        assertEquals(2, found.size());
        assertTrue(found.containsAll(Arrays.asList("%foo%", "%bar%")));
    }

    @Test
    void testTranslatePlaceholders_withoutAnyRegistered() {
        String input = "Just some text %nothing%";
        assertEquals(input, handler.translatePlaceholders(input));
    }

    @Test
    void testTranslateGlobalStaticPlaceholder() {
        handler.registerGlobalPlaceholder(new StaticPlaceholder("hello", () -> "WORLD"));
        String before = "Say %hello%!";
        assertEquals("Say WORLD!", handler.translatePlaceholders(before));
    }

    @Test
    void testTranslateWithContextPlaceholderOverridesGlobal() {
        handler.registerGlobalPlaceholder(new StaticPlaceholder("name", () -> "GLOBAL"));
        StaticPlaceholder local = new StaticPlaceholder("name", () -> "LOCAL");
        PlaceholderList list = new PlaceholderList() {
            @Override public void addPlaceholder(@NotNull Iterable<Placeholder> p, boolean d) {}
            @Override public void removePlaceholder(@NotNull Iterable<Placeholder> p, boolean d) {}
            @Override public void clearPlaceholders(boolean d) {}
            @Override public @NotNull List<Placeholder> getPlaceholders() { return Arrays.asList(local); }
        };
        PlaceholderContext ctx = new PlaceholderContext(list);
        assertEquals("User: LOCAL",
                handler.translatePlaceholders("User: %name%", ctx));
    }

    @Test
    void testDynamicPlaceholder_doubleNumbers() {
        Pattern inner = Pattern.compile("num:(\\d+)");
        DynamicPlaceholder dp = new DynamicPlaceholder(inner, replacing -> {
            Matcher m = Pattern.compile("%num:(\\d+)%").matcher(replacing);
            if (!m.find()) return null;
            int v = Integer.parseInt(m.group(1));
            return String.valueOf(v * 2);
        });
        handler.registerGlobalPlaceholder(dp);
        assertEquals("Double this: 14!",
                handler.translatePlaceholders("Double this: %num:7%!"));
    }

    @Test
    void testTryTranslateQuickly_onStaticPlaceholder() {
        StaticPlaceholder sp = new StaticPlaceholder("quick", () -> "FAST");
        assertEquals("Start FAST end",
                sp.tryTranslateQuickly("Start %quick% end", PlaceholderContext.EMPTY));
    }

    @Test
    void mergedContext_shouldContainBothGlobalAndLocal() {
        handler.registerGlobalPlaceholder(new StaticPlaceholder("g", () -> "GLOBAL"));
        StaticPlaceholder local = new StaticPlaceholder("l", () -> "LOCAL");
        PlaceholderList localList = new PlaceholderList() {
            @Override public void addPlaceholder(@NotNull Iterable<Placeholder> p, boolean d) {}
            @Override public void removePlaceholder(@NotNull Iterable<Placeholder> p, boolean d) {}
            @Override public void clearPlaceholders(boolean d) {}
            @Override public @NotNull List<Placeholder> getPlaceholders() { return Arrays.asList(local); }
        };
        PlaceholderContext merged = new PlaceholderContext(PlaceholderList.EMPTY)
                .withContext(localList);
        assertEquals("GLOBAL & LOCAL",
                handler.translatePlaceholders("%g% & %l%", merged));
    }

    @Test
    void localContext_shouldOverrideGlobal() {
        handler.registerGlobalPlaceholder(new StaticPlaceholder("x", () -> "G"));
        StaticPlaceholder localX = new StaticPlaceholder("x", () -> "L");
        PlaceholderList localList = new PlaceholderList() {
            @Override public void addPlaceholder(@NotNull Iterable<Placeholder> p, boolean d) {}
            @Override public void removePlaceholder(@NotNull Iterable<Placeholder> p, boolean d) {}
            @Override public void clearPlaceholders(boolean d) {}
            @Override public @NotNull List<Placeholder> getPlaceholders() { return Arrays.asList(localX); }
        };
        PlaceholderContext merged = new PlaceholderContext(PlaceholderList.EMPTY)
                .withContext(localList);
        assertEquals("L", handler.translatePlaceholders("%x%", merged));
    }

    @Test
    void createConfigFromString_shouldReplaceStaticPlaceholder() throws IOException {
        handler.registerGlobalPlaceholder(new StaticPlaceholder("who", () -> "World"));
        String raw = mkMapping("greet", "Hello %who%");
        Config cfg = manager.createConfigFromString(TestHelper.CONFIG_TYPE, raw);
        assertEquals("Hello World", cfg.getFormattedString("greet"));
    }

    @Test
    void formattedStringList_shouldReplacePlaceholdersInList() throws IOException {
        handler.registerGlobalPlaceholder(new StaticPlaceholder("one", () -> "1"));
        handler.registerGlobalPlaceholder(new StaticPlaceholder("two", () -> "2"));
        String raw = mkListMapping("vals", Arrays.asList("%one%", "%two%", "done"));
        Config cfg = manager.createConfigFromString(TestHelper.CONFIG_TYPE, raw);
        assertEquals(Arrays.asList("1","2","done"), cfg.getFormattedStringList("vals"));
    }

    @Test
    void createConfigFile_onDisk_shouldAlsoHonorPlaceholders() throws IOException {
        Path f = tmpRoot.resolve("test" + TestHelper.FILE_EXT);
        Files.write(
                f,
                mkMapping("x", "%foo%!").getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
        handler.registerGlobalPlaceholder(new StaticPlaceholder("foo", () -> "bar"));
        ConfigFile cf = manager.createConfigFile(
                TestHelper.CONFIG_TYPE, "test", Paths.get("test" + TestHelper.FILE_EXT), false
        );
        assertEquals("bar!", cf.getFormattedString("x"));
    }

    @Test
    void testTranslateConcurrently_noExceptions() throws InterruptedException {
        handler.registerGlobalPlaceholder(new StaticPlaceholder("t", () -> "THREAD"));
        Runnable r = () -> {
            for (int i=0; i<1_000; i++) {
                assertEquals("Value is THREAD",
                        handler.translatePlaceholders("Value is %t%"));
            }
        };
        Thread[] ts = new Thread[8];
        for (int i=0;i<ts.length;i++) {
            ts[i] = new Thread(r);
            ts[i].start();
        }
        for (Thread t:ts) t.join();
    }

    @Test
    void testRemoveAndClearPlaceholders() throws IOException {
        Config cfg = manager.createConfigFromString(
                TestHelper.CONFIG_TYPE,
                mkMapping("val", "%x%")
        );
        StaticPlaceholder sp = new StaticPlaceholder("x", () -> "REPLACED");
        cfg.addPlaceholders(true, sp);
        assertEquals("REPLACED", cfg.getFormattedString("val"));
        cfg.removePlaceholder(Arrays.asList(sp), true);
        assertEquals("%x%", cfg.getFormattedString("val"));
        cfg.addPlaceholders(true, sp);
        cfg.clearPlaceholders(true);
        assertEquals("%x%", cfg.getFormattedString("val"));
    }

    @Test
    void testShallowVsDeepInjection() throws IOException {
        String raw = mkNestedMapping("outer", "inner", "Hello %u%!");
        Config cfgShallow = manager.createConfigFromString(TestHelper.CONFIG_TYPE, raw);
        Config cfgDeep    = manager.createConfigFromString(TestHelper.CONFIG_TYPE, raw);
        StaticPlaceholder user = new StaticPlaceholder("u", () -> "User");

        cfgShallow.addPlaceholders(false, user);
        assertEquals("Hello %u%!",
                cfgShallow.getSubsection("outer").getFormattedString("inner"));

        cfgDeep.addPlaceholders(true, user);
        assertEquals("Hello User!",
                cfgDeep.getSubsection("outer").getFormattedString("inner"));
    }

    //――――――――――――――――――――――――――――――――――――――――――――――
    // helpers to generate JSON or YAML
    //――――――――――――――――――――――――――――――――――――――――――――――
    private static String mkMapping(String key, String value) {
        switch (TestHelper.CONFIG_TYPE) {
            case JSON:
                return "{\"" + key + "\": \"" + value + "\"}";
            case YAML:
                return key + ": \"" + value + "\"\n";
            default:
                throw new IllegalStateException("Unsupported: " + TestHelper.CONFIG_TYPE);
        }
    }

    private static String mkNestedMapping(String outer, String inner, String val) {
        switch (TestHelper.CONFIG_TYPE) {
            case JSON:
                return "{\"" + outer + "\": {\"" + inner + "\": \"" + val + "\"}}";
            case YAML:
                return outer + ":\n"
                        + "  " + inner + ": \"" + val + "\"\n";
            default:
                throw new IllegalStateException("Unsupported: " + TestHelper.CONFIG_TYPE);
        }
    }

    private static String mkListMapping(String key, List<String> items) {
        if (TestHelper.CONFIG_TYPE == ConfigType.JSON) {
            StringBuilder sb = new StringBuilder();
            sb.append("{\"").append(key).append("\":[");
            for (int i=0;i<items.size();i++) {
                sb.append("\"").append(items.get(i)).append("\"");
                if (i < items.size()-1) sb.append(",");
            }
            sb.append("]}");
            return sb.toString();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(key).append(":\n");
            for (String it : items) {
                sb.append("  - \"").append(it).append("\"\n");
            }
            return sb.toString();
        }
    }
}
