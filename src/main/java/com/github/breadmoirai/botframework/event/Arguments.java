package com.github.breadmoirai.botframework.event;

import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class Arguments {

    private static final Pattern FLOAT_REGEX = Pattern.compile("[+-]?(" +
            "NaN|" +
            "Infinity|" +
            "((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)([eE][+-]?(\\p{Digit}+))?)|" +
            "(\\.(\\p{Digit}+)([eE][+-]?(\\p{Digit}+))?)|" +
            "((" +
            "(0[xX](\\p{XDigit}+)(\\.)?)|" +
            "(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+))" +
            ")[pP][+-]?(\\p{Digit}+)))" +
            "[fFdD]?))");


    private Arguments() {
    }

    public static boolean isNumber(String s) {
        if (s == null) return false;
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (i == 0 && s.charAt(i) == '-') {
                if (s.length() == 1) return false;
                else continue;
            }
            if (Character.digit(s.charAt(i), 10) < 0) return false;
        }
        return true;
    }

    public static boolean isInteger(String s) {
        if (s == null) return false;
        if (s.isEmpty()) return false;
        int neg = s.charAt(0) == '-' ? 1 : 0;
        int length = s.length();
        if (length > 10 + neg) return false;
        if (length < 10 + neg) return isNumber(s);
        for (int i = 0; i < length; i++) {
            if (i == 0 && neg == 1) {
                if (length == 1) return false;
                else continue;
            }
            int digit = Character.digit(s.charAt(i), 10);
            if (digit < 0) return false;
            else {
                switch (neg == 0 ? i : i - 1) {
                    case 0:
                        if (digit > 2) return false;
                        break;
                    case 1:
                        if (digit > 1) return false;
                        break;
                    case 2:
                        if (digit > 4) return false;
                        break;
                    case 3:
                        if (digit > 7) return false;
                        break;
                    case 4:
                        if (digit > 4) return false;
                        break;
                    case 5:
                        if (digit > 8) return false;
                        break;
                    case 6:
                        if (digit > 3) return false;
                        break;
                    case 7:
                        if (digit > 6) return false;
                        break;
                    case 8:
                        if (digit > 4) return false;
                        break;
                    case 9:
                        if (digit > 7 + neg) return false;
                        break;
                }
            }
        }
        return true;
    }

    public static boolean isLong(String s) {
        if (s == null) return false;
        if (s.isEmpty()) return false;
        int neg = s.charAt(0) == '-' ? 1 : 0;
        int length = s.length();
        if (length > 19 + neg) return false;
        if (length < 19 + neg) return isNumber(s);
        for (int i = 0; i < length; i++) {
            if (i == 0 && neg == 1) {
                if (length == 1) return false;
                else continue;
            }
            int digit = Character.digit(s.charAt(i), 10);
            if (digit < 0) return false;
            else {
                switch (neg == 0 ? i : i - 1) {
                    case 0:
                        if (digit > 9) return false;
                        break;
                    case 1:
                        if (digit > 2) return false;
                        break;
                    case 2:
                        if (digit > 2) return false;
                        break;
                    case 3:
                        if (digit > 3) return false;
                        break;
                    case 4:
                        if (digit > 3) return false;
                        break;
                    case 5:
                        if (digit > 7) return false;
                        break;
                    case 6:
                        if (digit > 2) return false;
                        break;
                    case 7:
                        if (digit > 0) return false;
                        break;
                    case 8:
                        if (digit > 3) return false;
                        break;
                    case 9:
                        if (digit > 6) return false;
                        break;
                    case 10:
                        if (digit > 8) return false;
                        break;
                    case 11:
                        if (digit > 5) return false;
                        break;
                    case 12:
                        if (digit > 4) return false;
                        break;
                    case 13:
                        if (digit > 7) return false;
                        break;
                    case 14:
                        if (digit > 7) return false;
                        break;
                    case 15:
                        if (digit > 5) return false;
                        break;
                    case 16:
                        if (digit > 8) return false;
                        break;
                    case 17:
                        if (digit > 0) return false;
                        break;
                    case 18:
                        if (digit > 7 + neg) return false;
                        break;
                }
            }
        }
        return true;
    }

    public static boolean isFloat(String s) {
        return FLOAT_REGEX.matcher(s).matches();
    }

    public static boolean isDouble(String s) {
        return isFloat(s);
    }

    public static boolean isRange(String s) {
        int neg1 = s.charAt(0) == '-' ? 1 : 0;
        int dash = s.indexOf('-', neg1);
        if (dash == -1 || dash + 1 == s.length()) return false;
        String s1 = s.substring(0, dash);
        String s2 = s.substring(dash + 1, s.length());
        return isInteger(s1) && isInteger(s2);
    }

    public static IntStream parseRange(String s) {
        int neg1 = s.charAt(0) == '-' ? 1 : 0;
        int dash = s.indexOf('-', neg1);
        if (dash == -1 && isInteger(s)) {
            return IntStream.of(Integer.parseInt(s));
        }
        if (dash == s.length() - 1) {
            return IntStream.empty();
        }
        String sa = s.substring(0, dash);
        String sb = s.substring(dash + 1, s.length());
        if (!isInteger(s) || !isInteger(sb)) return IntStream.empty();
        int a = Integer.parseInt(sa);
        int b = Integer.parseInt(sb);
        if (a < b) {
            return IntStream.rangeClosed(a, b);
        } else {
            return IntStream.rangeClosed(b, a)
                    .map(i -> a - i + b);
        }
    }

    public static boolean isHex(String s) {
        if (s == null) return false;
        if (s.isEmpty()) return false;
        int prefix = 0;
        if (s.startsWith("0x") || s.startsWith("0X")) {
            prefix = 2;
        }
        if (s.startsWith("#")) {
            prefix = 1;
        }
        for (int i = prefix; i < s.length(); i++) {
            if (Character.digit(s.charAt(i), 16) < 0) return false;
        }
        return true;
    }

    public static boolean isMention(String s) {
        return s.startsWith("<") && s.endsWith(">");
    }

    public static boolean isAlphanumericWithUnderscoresOrDashesWithAMinimumLengthOf2AndAMaximumLengthOf32(String name) {
        int length = name.length();
        if (length < 2 || length > 32) return false;
        for (int i = 0; i < length; i++) {
            char c = name.charAt(i);
            if (!Character.isDigit(c) && !Character.isAlphabetic(c) && c != '-' && c != '_') return false;
        }
        return true;
    }
}
