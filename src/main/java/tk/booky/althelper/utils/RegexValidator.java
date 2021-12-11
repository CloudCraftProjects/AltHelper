package tk.booky.althelper.utils;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexValidator implements Serializable {

    private static final long serialVersionUID = -8832409930574867162L;
    private final Pattern[] patterns;

    public RegexValidator(String regex) {
        this(regex, true);
    }

    public RegexValidator(String regex, boolean caseSensitive) {
        this(new String[]{regex}, caseSensitive);
    }

    public RegexValidator(String[] regexes, boolean caseSensitive) {
        if (regexes == null || regexes.length == 0)
            throw new IllegalArgumentException("Regular expressions are missing");

        patterns = new Pattern[regexes.length];
        int flags = (caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
        for (int i = 0; i < regexes.length; i++) {
            if (regexes[i] == null || regexes[i].length() == 0)
                throw new IllegalArgumentException("Regular expression[" + i + "] is missing");
            patterns[i] = Pattern.compile(regexes[i], flags);
        }
    }

    public String[] match(String value) {
        if (value == null) return null;
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(value);
            if (matcher.matches()) {
                int count = matcher.groupCount();
                String[] groups = new String[count];
                for (int j = 0; j < count; j++)
                    groups[j] = matcher.group(j + 1);
                return groups;
            }
        }
        return null;
    }
}