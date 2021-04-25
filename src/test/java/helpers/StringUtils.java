package helpers;

import java.nio.file.FileSystems;

public class StringUtils {
    public static final String SPT = FileSystems.getDefault().getSeparator();
    private StringUtils() {
    }

    public static String concatPathToFile(String... names) {
        return concatPath(true, names);
    }

    public static String concatPath(String... names) {
        return concatPath(false, names);
    }

    private static String concatPath(boolean toFile, String... names) {
        StringBuilder result = new StringBuilder();
        for (String str : names) {
            result.append(str);
            if (!str.endsWith(SPT)) {
                result.append(SPT);
            }
        }
        if (toFile) {
            result.setLength(result.length() - SPT.length());
        }
        return result.toString();
    }
}
