package com.evolunteer.utils;

import java.util.List;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * CSV 导出工具类：把导出数据按 RFC 4180 规则拼装为逗号分隔文本，
 * 对含逗号、引号与换行的字段加引号转义，并在文件开头写入 UTF-8 字节序标记，便于直接用表格软件打开。
 */
public final class CsvExporter {

    /**
     * UTF-8 字节序标记，Excel 依赖它识别文件编码
     */
    private static final String UTF8_BOM = "\uFEFF";

    /**
     * 换行符
     */
    private static final String LINE_SEPARATOR = "\r\n";

    private CsvExporter() {
    }

    /**
     * 生成 CSV 文本
     *
     * @param headers 表头
     * @param rows    数据行
     * @return 带 UTF-8 字节序标记的 CSV 文本
     */
    public static String toCsv(List<String> headers, List<List<String>> rows) {

        StringBuilder csv = new StringBuilder(UTF8_BOM);
        appendRow(csv, headers);
        for (List<String> row : rows) {
            appendRow(csv, row);
        }
        return csv.toString();
    }

    /**
     * 追加一行数据
     */
    private static void appendRow(StringBuilder csv, List<String> cells) {
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) {
                csv.append(',');
            }
            csv.append(escape(cells.get(i)));
        }
        csv.append(LINE_SEPARATOR);
    }

    /**
     * 转义单个单元格：含逗号、引号、回车或换行时整体加引号，内部引号双写
     *
     * @param value 原始值
     * @return 转义后的单元格文本
     */
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
