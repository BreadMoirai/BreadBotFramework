package com.github.breadmoirai.framework.event;

import java.util.regex.Pattern;

public class Arguments {

    private static Pattern FloatValue = Pattern.compile("[+-]?(" +
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

    public static boolean isDouble(String s) {
        return FloatValue.matcher(s).matches();
    }

    public static boolean isInteger(String string) {
        return false;
    }
}
