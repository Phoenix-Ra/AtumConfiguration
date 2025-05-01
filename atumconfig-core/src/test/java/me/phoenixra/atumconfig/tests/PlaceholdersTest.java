package me.phoenixra.atumconfig.tests;

import me.phoenixra.atumconfig.api.ConfigLogger;
import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.atumconfig.api.config.ConfigType;
import me.phoenixra.atumconfig.api.placeholders.Placeholder;
import me.phoenixra.atumconfig.api.placeholders.context.PlaceholderContext;
import me.phoenixra.atumconfig.api.placeholders.context.PlaceholderList;
import me.phoenixra.atumconfig.api.placeholders.types.DynamicPlaceholder;
import me.phoenixra.atumconfig.api.placeholders.types.StaticPlaceholder;
import me.phoenixra.atumconfig.core.AtumConfigManager;
import me.phoenixra.atumconfig.core.AtumPlaceholderHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlaceholdersTest {

    private AtumPlaceholderHandler handler;

    @TempDir
    Path tmpRoot;
    private ConfigManager manager;

    @BeforeEach
    void setUp() {

        handler = new AtumPlaceholderHandler(ConfigLogger.EMPTY);

        manager = new AtumConfigManager("test", tmpRoot, true);
        manager.setPlaceholderHandler(handler);
    }

    @Test
    void testFindPlaceholdersIn_emptyAndNonMatching() {
        assertTrue(handler.findPlaceholdersIn("no placeholders here").isEmpty());
        // adjacent percent signs should not match
        assertTrue(handler.findPlaceholdersIn("%%not a placeholder%%").isEmpty());
    }

    @Test
    void testFindPlaceholdersIn_multiple() {
        String text = "Hello %foo% and %bar%!";
        List<String> found = handler.findPlaceholdersIn(text);
        assertEquals(2, found.size());
        assertTrue(found.containsAll(List.of("%foo%", "%bar%")));
    }

    @Test
    void testTranslatePlaceholders_withoutAnyRegistered() {
        // with no placeholders registered, text should be unchanged
        String input = "Just some text %nothing%";
        assertEquals(input, handler.translatePlaceholders(input));
    }

    @Test
    void testTranslateGlobalStaticPlaceholder() {
        // register a static placeholder that always returns "WORLD"
        handler.registerGlobalPlaceholder(new StaticPlaceholder("hello", () -> "WORLD"));

        String before = "Say %hello%!";
        String after = handler.translatePlaceholders(before);
        assertEquals("Say WORLD!", after);
    }

    @Test
    void testTranslateWithContextPlaceholderOverridesGlobal() {
        // global placeholder returns "GLOBAL"
        StaticPlaceholder global = new StaticPlaceholder("name", () -> "GLOBAL");
        handler.registerGlobalPlaceholder(global);

        // context placeholder for %name% returns "LOCAL"
        StaticPlaceholder local = new StaticPlaceholder("name", () -> "LOCAL");
        PlaceholderList list = new PlaceholderList() {
            @Override
            public void addPlaceholder(Iterable<Placeholder> placeholders, boolean deep) { }
            @Override
            public void removePlaceholder(Iterable<Placeholder> placeholders, boolean deep) { }
            @Override
            public void clearPlaceholders(boolean deep) { }
            @Override
            public List<Placeholder> getPlaceholders() { return List.of(local); }
        };
        PlaceholderContext ctx = new PlaceholderContext(list);

        String before = "User: %name%";
        String translated = handler.translatePlaceholders(before, ctx);
        assertEquals("User: LOCAL", translated);
    }

    @Test
    void testDynamicPlaceholder_doubleNumbers() {
        // inner pattern matches e.g. "num:5"
        Pattern inner = Pattern.compile("num:(\\d+)");
        DynamicPlaceholder dp = new DynamicPlaceholder(inner, replacing -> {
            Matcher m = Pattern.compile("%num:(\\d+)%").matcher(replacing);
            if (!m.find()) return null;
            int val = Integer.parseInt(m.group(1));
            return String.valueOf(val * 2);
        });
        handler.registerGlobalPlaceholder(dp);

        String input = "Double this: %num:7%!";
        String output = handler.translatePlaceholders(input);
        assertEquals("Double this: 14!", output);
    }

    @Test
    void testTryTranslateQuickly_onStaticPlaceholder() {
        StaticPlaceholder sp = new StaticPlaceholder("quick", () -> "FAST");
        String source = "Start %quick% end";
        String result = sp.tryTranslateQuickly(source, PlaceholderContext.EMPTY);
        assertEquals("Start FAST end", result);
    }


    @Test
    void mergedContext_shouldContainBothGlobalAndLocal() {
        // global placeholder %g% -> "GLOBAL"
        StaticPlaceholder global = new StaticPlaceholder("g", () -> "GLOBAL");
        handler.registerGlobalPlaceholder(global);

        // local/context placeholder %l% -> "LOCAL"
        StaticPlaceholder local = new StaticPlaceholder("l", () -> "LOCAL");

        // stub list that only ever returns our `local`
        PlaceholderList localList = new PlaceholderList() {
            @Override public void addPlaceholder(Iterable<Placeholder> p, boolean d) { }
            @Override public void removePlaceholder(Iterable<Placeholder> p, boolean d) { }
            @Override public void clearPlaceholders(boolean d) { }
            @Override public List<Placeholder> getPlaceholders() {
                return List.of(local);
            }
        };

        PlaceholderContext merged = new PlaceholderContext(PlaceholderList.EMPTY)
                .withContext(localList);

        String template = "%g% & %l%";
        String result = handler.translatePlaceholders(template, merged);

        assertEquals("GLOBAL & LOCAL", result);
    }

    @Test
    void localContext_shouldOverrideGlobal() {
        // global %x% -> "G"
        handler.registerGlobalPlaceholder(new StaticPlaceholder("x", () -> "G"));
        // local %x% -> "L"
        StaticPlaceholder localX = new StaticPlaceholder("x", () -> "L");
        PlaceholderList localList = new PlaceholderList() {
            @Override public void addPlaceholder(Iterable<Placeholder> p, boolean d) { }
            @Override public void removePlaceholder(Iterable<Placeholder> p, boolean d) { }
            @Override public void clearPlaceholders(boolean d) { }
            @Override public List<Placeholder> getPlaceholders() {
                return List.of(localX);
            }
        };
        PlaceholderContext merged = new PlaceholderContext(PlaceholderList.EMPTY)
                .withContext(localList);

        assertEquals("L", handler.translatePlaceholders("%x%", merged));
    }




    @Test
    void createConfigFromString_shouldReplaceStaticPlaceholder() throws IOException {
        handler.registerGlobalPlaceholder(new StaticPlaceholder("who", () -> "World"));

        // our JSON has a key "greet":"Hello %who%"
        Config cfg = manager.createConfigFromString(
                ConfigType.JSON,
                "{\"greet\": \"Hello %who%\"}"
        );

        assertEquals("Hello World", cfg.getFormattedString("greet"));
    }

    @Test
    void formattedStringList_shouldReplacePlaceholdersInList() throws IOException {
        handler.registerGlobalPlaceholder(new StaticPlaceholder("one", () -> "1"));
        handler.registerGlobalPlaceholder(new StaticPlaceholder("two", () -> "2"));

        Config cfg = manager.createConfigFromString(
                ConfigType.JSON,
                "{\"vals\":[\"%one%\",\"%two%\",\"done\"]}"
        );

        List<String> out = cfg.getFormattedStringList("vals");
        assertEquals(List.of("1","2","done"), out);
    }

    @Test
    void createConfigFile_onDisk_shouldAlsoHonorPlaceholders() throws IOException {
        // write a template resource into tmpRoot/test.json:
        Path file = tmpRoot.resolve("test.json");
        Files.writeString(file, "{\"x\":\"%foo%!\"}");

        handler.registerGlobalPlaceholder(new StaticPlaceholder("foo", () -> "bar"));

        ConfigFile cf = manager.createConfigFile(
                ConfigType.JSON,
                "test",
                Path.of("test.json"),
                false
        );

        // reading from disk + formatting:
        assertEquals("bar!", cf.getFormattedString("x"));
    }



    @Test
    void testTranslateConcurrently_noExceptions() throws InterruptedException {
        handler.registerGlobalPlaceholder(new StaticPlaceholder("t", () -> "THREAD"));

        String template = "Value is %t%";
        Runnable r = () -> {
            for (int i = 0; i < 1_000; i++) {
                String out = handler.translatePlaceholders(template);
                assertEquals("Value is THREAD", out);
            }
        };

        Thread[] threads = new Thread[8];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(r);
            threads[i].start();
        }
        for (Thread t : threads) {
            t.join();
        }
    }

    @Test
    void testRemoveAndClearPlaceholders() throws IOException {
        // prepare a simple config
        Config cfg = manager.createConfigFromString(
                ConfigType.JSON,
                "{\"val\":\"%x%\"}"
        );
        StaticPlaceholder sp = new StaticPlaceholder("x", () -> "REPLACED");

        // inject
        cfg.addPlaceholders(true, sp);
        assertEquals("REPLACED", cfg.getFormattedString("val"));

        // remove
        cfg.removePlaceholder(List.of(sp), true);
        assertEquals("%x%", cfg.getFormattedString("val"));

        // inject again and then clear
        cfg.addPlaceholders(true, sp);
        assertEquals("REPLACED", cfg.getFormattedString("val"));
        cfg.clearPlaceholders(true);
        assertEquals("%x%", cfg.getFormattedString("val"));
    }

    @Test
    void testShallowVsDeepInjection() throws IOException {
        // JSON with nested section
        String raw = "{\"outer\": {\"inner\": \"Hello %u%!\"}}";
        Config cfgShallow = manager.createConfigFromString(ConfigType.JSON, raw);
        Config cfgDeep    = manager.createConfigFromString(ConfigType.JSON, raw);

        StaticPlaceholder userPl = new StaticPlaceholder("u", () -> "User");

        // shallow
        cfgShallow.addPlaceholders(false, userPl);
        // shallow should *not* apply inside nested "outer"
        assertEquals("Hello %u%!",
                cfgShallow.getSubsection("outer")
                        .getFormattedString("inner")
        );

        // deep
        cfgDeep.addPlaceholders(true, userPl);
        assertEquals("Hello User!",
                cfgDeep.getSubsection("outer")
                        .getFormattedString("inner")
        );
    }
}
