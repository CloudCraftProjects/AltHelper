package dev.booky.alts.util;
// Created by booky10 in AltHelper (17:56 11.05.22)

public class WildcardMatcher {

    public static boolean matches(String str, String expr) {
        int indexOfWildcard = expr.indexOf('*');
        if (indexOfWildcard == -1) return str.equalsIgnoreCase(expr);

        boolean multipleWildcards = expr.lastIndexOf('*') != indexOfWildcard;
        if (multipleWildcards) throw new UnsupportedOperationException("Multiple wildcards are not supported yet");

        String prefix = expr.substring(0, indexOfWildcard);
        String suffix = expr.substring(indexOfWildcard + 1);

        if (prefix.isEmpty() && suffix.isEmpty()) return true;

        String lowerStr = str.toLowerCase();
        if (prefix.isEmpty()) return lowerStr.endsWith(suffix.toLowerCase());
        if (suffix.isEmpty()) return lowerStr.startsWith(prefix.toLowerCase());
        return lowerStr.startsWith(prefix.toLowerCase()) && lowerStr.endsWith(suffix.toLowerCase());
    }
}
