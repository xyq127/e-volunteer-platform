package com.evolunteer.utils;

import java.util.List;

public final class CsvExporter {

    private static final String UTF8_BOM = "\uFEFF";

    private static final String LINE_SEPARATOR = "\r\n";

    private CsvExporter() {
    }

    public static String toCsv(List<String> headers, List<List<String>> rows) {

        StringBuilder csv = new StringBuilder(UTF8_BOM);
        appendRow(csv, headers);
        for (List<String> row : rows) {
            appendRow(csv, row);
        }
        return csv.toString();
    }

    private static void appendRow(StringBuilder csv, List<String> cells) {
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) {
                csv.append(',');
            }
            csv.append(escape(cells.get(i)));
        }
        csv.append(LINE_SEPARATOR);
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.indexOf(',') < 0 && value.indexOf('"') < 0
                && value.indexOf('\n') < 0 && value.indexOf('\r') < 0) {
            return value;
        }
        return '"' + value.replace("\"", "\"\"") + '"';
    }
}
