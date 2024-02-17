package me.phoenixra.atumconfig.api.utils;


import me.phoenixra.atumconfig.api.ConfigOwner;
import me.phoenixra.atumconfig.api.placeholders.PlaceholderManager;
import me.phoenixra.atumconfig.api.placeholders.context.PlaceholderContext;
import me.phoenixra.atumconfig.tuples.PairRecord;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * StringUtils
 */
public class StringUtils {




    /**
     * Format a string with color codes. (for Minecraft)
     *
     * @param text The text to format.
     * @return The formatted string.
     */
    @NotNull
    public static String formatMinecraftColors(@NotNull String text) {
        return replaceFast(text, "&", "§");
    }

    /**
     * Format a list of strings with color codes. (for Minecraft)
     *
     * @param list The list to format.
     * @return The formatted list.
     */
    @NotNull
    public static Collection<String> formatMinecraftColors(@NotNull Collection<String> list) {
        Collection<String> output= new ArrayList<>();
        for (String entry : list) {
            output.add(formatMinecraftColors(entry));
        }
        return output;
    }

    /**
     * Format a string with color codes and placeholders.
     *
     * @param configOwner The config owner.
     * @param text The text to format.
     * @param context The placeholder context.
     * @return The formatted string.
     */
    @NotNull
    public static String formatWithPlaceholders(@NotNull ConfigOwner configOwner,
                                                @NotNull String text,
                                                @NotNull PlaceholderContext context) {
        return PlaceholderManager.translatePlaceholders(configOwner,
                text, context);
    }

    /**
     * Format a list of strings with color codes and placeholders.
     *
     * @param configOwner The config owner.
     * @param list The list to format.
     * @param context The placeholder context.
     * @return The formatted list.
     */
    @NotNull
    public static List<String> formatWithPlaceholders(@NotNull ConfigOwner configOwner,
                                                      @NotNull Collection<String> list,
                                                      @NotNull PlaceholderContext context) {
        List<String> out = new ArrayList<>();
        for(String s : list){
            out.add(PlaceholderManager.translatePlaceholders(configOwner,
                    s,context));
        }
        return out;
    }

    /**
     * Remove color codes from a string.
     *
     * @param input The input string.
     * @return The string without color codes.
     */
    @NotNull
    public static String removeColorCodes(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);
            if (currentChar == '\u00A7' || currentChar == '&') {
                i++;
            } else {
                result.append(currentChar);
            }
        }
        return result.toString();
    }

    /**
     * Fast implementation of {@link String#replace(CharSequence, CharSequence)}
     *
     * @param input       The input string.
     * @param placeholder The placeholder pair.
     * @return The replaced string.
     */
    @NotNull
    public static String replaceFast(@NotNull final String input,
                                     @NotNull final List<PairRecord<String,String>> placeholder) {
        String out = input;
        for (PairRecord<String,String> pair : placeholder) {
            out = replaceFast(out, pair.getFirst(), pair.getSecond());
        }
        return out;
    }

    /**
     * Fast implementation of {@link String#replace(CharSequence, CharSequence)}
     *
     * @param input       The input string.
     * @param target      The target string.
     * @param replacement The replacement string.
     * @return The replaced string.
     */
    @NotNull
    public static String replaceFast(@NotNull final String input,
                                     @NotNull final String target,
                                     @NotNull final String replacement) {
        int targetLength = target.length();

        // Count the number of original occurrences
        int count = 0;
        for (
                int index = input.indexOf(target);
                index != -1;
                index = input.indexOf(target, index + targetLength)
        ) {
            count++;
        }

        if (count == 0) {
            return input;
        }

        int replacementLength = replacement.length();
        int inputLength = input.length();

        // Pre-calculate the final size of the StringBuilder
        int newSize = inputLength + (replacementLength - targetLength) * count;
        StringBuilder result = new StringBuilder(newSize);

        int start = 0;
        for (
                int index = input.indexOf(target);
                index != -1;
                index = input.indexOf(target, start)
        ) {
            result.append(input, start, index);
            result.append(replacement);
            start = index + targetLength;
        }

        result.append(input, start, inputLength);
        return result.toString();
    }

    /**
     * Parse string into tokens.
     * <p>
     * Handles quoted strings for names.
     *
     * @param lookup The lookup string.
     * @return An array of tokens to be processed.
     * @author Shawn (<a href="https://stackoverflow.com/questions/70606170/split-a-list-on-spaces-and-group-quoted-characters/70606653#70606653">...</a>)
     */
    @NotNull
    public static String[] parseTokens(@NotNull final String lookup) {
        char[] chars = lookup.toCharArray();
        List<String> tokens = new ArrayList<>();
        StringBuilder tokenBuilder = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == ' ') {
                /*
                Take the current value of the argument builder, append it to the
                list of found tokens, and then clear it for the next argument.
                 */
                tokens.add(tokenBuilder.toString());
                tokenBuilder.setLength(0);
            } else if (chars[i] == '"') {
                /*
                Work until the next unescaped quote to handle quotes with
                spaces in them - assumes the input string is well-formatted
                 */
                for (i++; chars[i] != '"'; i++) {
                    /*
                    If the found quote is escaped, ignore it in the parsing
                     */
                    if (chars[i] == '\\') {
                        i++;
                    }
                    tokenBuilder.append(chars[i]);
                }
            } else {
                /*
                If it's a regular character, just append it to the current argument.
                 */
                tokenBuilder.append(chars[i]);
            }
        }
        tokens.add(tokenBuilder.toString()); // Adds the last argument to the tokens.
        return tokens.toArray(new String[0]);
    }

    /**
     * Get a string's margin.
     *
     * @param input The input string.
     * @return The margin.
     */
    public static int getMargin(@NotNull final String input) {
        return input.indexOf(input.trim());
    }

    /**
     * Better implementation of {@link Object#toString()}.
     *
     * @param object The object to convert.
     * @return The nice string.
     */
    public static String toNiceString(Object object) {
        if (object == null) {
            return "null";
        }
        if (object instanceof Integer) {
            return ((Integer) object).toString();
        } else if (object instanceof String) {
            return (String) object;
        } else if (object instanceof Double) {
            return NumberUtils.format((Double) object);
        } else if (object instanceof Collection<?>) {
            return ((Collection<?>)object).stream().map(StringUtils::toNiceString).collect(Collectors.joining(", "));
        } else {
            return String.valueOf(object);
        }
    }

    private StringUtils() {

        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }
}
