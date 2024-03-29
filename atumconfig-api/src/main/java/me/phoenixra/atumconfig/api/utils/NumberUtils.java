package me.phoenixra.atumconfig.api.utils;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.TreeMap;

public class NumberUtils {
    private static final TreeMap<Integer, String> NUMERALS = new TreeMap<>();
    private static DecimalFormat doubleFormatter = new DecimalFormat("0.00");

    static {
        NUMERALS.put(1000, "M");
        NUMERALS.put(900, "CM");
        NUMERALS.put(500, "D");
        NUMERALS.put(400, "CD");
        NUMERALS.put(100, "C");
        NUMERALS.put(90, "XC");
        NUMERALS.put(50, "L");
        NUMERALS.put(40, "XL");
        NUMERALS.put(10, "X");
        NUMERALS.put(9, "IX");
        NUMERALS.put(5, "V");
        NUMERALS.put(4, "IV");
        NUMERALS.put(1, "I");
    }

    /**
     * Format double to string with 2 decimal places.
     *
     * @param toFormat The number to format.
     * @return Formatted.
     */
    @NotNull
    public static String format(final double toFormat) {
        String formatted = doubleFormatter.format(toFormat);

        return formatted.endsWith("00") ? String.valueOf((int) toFormat) : formatted;
    }

    /**
     * Get Roman Numeral from number.
     *
     * @param number The number to convert.
     * @return The number, converted to a roman numeral.
     */
    @NotNull
    public static String toNumeral(final int number) {
        return toNumeral(number, -1);
    }

    /**
     * Get Roman Numeral from number.
     *
     * @param number The number to convert.
     * @param romanNumberLimit The number after which it will be returned as arabic
     * @return The number, converted to a roman numeral.
     */
    @NotNull
    public static String toNumeral(final int number, int romanNumberLimit) {
        if(number > romanNumberLimit && romanNumberLimit != -1) return String.valueOf(number);
        if (number >= 1 && number <= 4096) {
            int l = NUMERALS.floorKey(number);
            if (number == l) {
                return NUMERALS.get(number);
            }
            return NUMERALS.get(l) + toNumeral(number - l);
        } else {
            return String.valueOf(number);
        }
    }

    /**
     * Get number from roman numeral.
     *
     * @param numeral The numeral to convert.
     * @return The number, converted from a roman numeral.
     */
    public static int fromNumeral(@NotNull final String numeral) {
        if (numeral.isEmpty()) {
            return 0;
        }
        for (Map.Entry<Integer, String> entry : NUMERALS.descendingMap().entrySet()) {
            if (numeral.startsWith(entry.getValue())) {
                return entry.getKey() + fromNumeral(numeral.substring(entry.getValue().length()));
            }
        }
        return 0;
    }

    /**
     * Get if is an integer or double.
     *
     * @param value The String to check
     * @return If is integer or double.
     */
    public static boolean isIntegerOrDouble(String value) {
        try {
            if (value.contains(".")) {
                Double.parseDouble(value);
            } else {
                Integer.parseInt(value);
            }
            return true;
        } catch (Exception e) {}
        return false;
    }

    /**
     * Get if is an integer.
     *
     * @param value The String to check
     * @return If is integer.
     */
    public static boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (Exception e) {}
        return false;
    }

    /**
     * Get if is a double.
     *
     * @param value The String to check
     * @return If is double.
     */
    public static boolean isDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (Exception e) {}
        return false;
    }

    /**
     * Get if is a long.
     *
     * @param value The String to check
     * @return If is long.
     */
    public static boolean isLong(String value) {
        try {
            Long.parseLong(value);
            return true;
        } catch (Exception e) {}
        return false;
    }

    /**
     * Get if is a float.
     *
     * @param value The String to check
     * @return If is float.
     */
    public static boolean isFloat(String value) {
        try {
            Float.parseFloat(value);
            return true;
        } catch (Exception e) {}
        return false;
    }
}
