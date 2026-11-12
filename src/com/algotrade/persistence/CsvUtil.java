
package com.algotrade.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class CsvUtil {

    private CsvUtil() {
        // Utility class
    }

    // Write CSV data (overwrites existing file)
    public static void write(
            Path filePath, List<String[]> rows) throws IOException {

        if (filePath == null || rows == null) {
            throw new IllegalArgumentException(
                "File path and rows are required");
        }

        Path parent = filePath.toAbsolutePath().getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        List<String> lines = new ArrayList<>();

        for (String[] row : rows) {
            if (row == null) {
                throw new IllegalArgumentException(
                    "CSV row cannot be null");
            }

            List<String> escaped = new ArrayList<>();

            for (String value : row) {
                escaped.add(escape(value));
            }

            lines.add(String.join(",", escaped));
        }

        Files.write(
            filePath,
            lines,
            StandardCharsets.UTF_8
        );
    }

    // Read CSV data
    public static List<String[]> read(Path filePath)
            throws IOException {

        if (filePath == null) {
            throw new IllegalArgumentException(
                "File path is required");
        }

        List<String[]> rows = new ArrayList<>();

        if (!Files.exists(filePath)) {
            return rows;
        }

        for (String line :
                Files.readAllLines(filePath, StandardCharsets.UTF_8)) {

            rows.add(parseLine(line));
        }

        return rows;
    }

    // Escape special CSV characters
    private static String escape(String value) {

        if (value == null) {
            return "";
        }

        if (value.contains(",") ||
                value.contains("\"") ||
                value.contains("\n") ||
                value.contains("\r")) {

            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }

    // Parse one CSV line
    private static String[] parseLine(String line) {

        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {

            char ch = line.charAt(i);

            if (ch == '"') {

                if (insideQuotes &&
                        i + 1 < line.length() &&
                        line.charAt(i + 1) == '"') {

                    current.append('"');
                    i++;

                } else {
                    insideQuotes = !insideQuotes;
                }

            } else if (ch == ',' && !insideQuotes) {

                values.add(current.toString());
                current.setLength(0);

            } else {
                current.append(ch);
            }
        }

        if (insideQuotes) {
            throw new IllegalArgumentException(
                "Malformed CSV: unclosed quotation mark");
        }

        values.add(current.toString());

        return values.toArray(new String[0]);
    }
}
