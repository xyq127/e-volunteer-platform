package com.evolunteer.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvExporterTest {

    @Test
    @DisplayName("导出内容带 UTF-8 字节序标记并使用回车换行分隔")
    void toCsvShouldWriteBomAndLineSeparator() {
        String csv = CsvExporter.toCsv(Arrays.asList("姓名", "时长"), Collections.singletonList(Arrays.asList("张三", "3.0")));

        assertTrue(csv.startsWith("\uFEFF"));
        assertEquals("\uFEFF姓名,时长\r\n张三,3.0\r\n", csv);
    }

    @Test
    @DisplayName("包含逗号、引号与换行的字段按 RFC 4180 规则转义")
    void escapeShouldQuoteSpecialCharacters() {
        assertEquals("普通文本", CsvExporter.escape("普通文本"));
        assertEquals("\"含,逗号\"", CsvExporter.escape("含,逗号"));
        assertEquals("\"含\"\"引号\"", CsvExporter.escape("含\"引号"));
        assertEquals("\"含换行\n文本\"", CsvExporter.escape("含换行\n文本"));
        assertEquals("", CsvExporter.escape(null));
    }

    @Test
    @DisplayName("空数据行只输出表头")
    void toCsvShouldHandleEmptyRows() {
        List<String> headers = Arrays.asList("活动编号", "活动名称");

        assertEquals("\uFEFF活动编号,活动名称\r\n", CsvExporter.toCsv(headers, Collections.emptyList()));
    }
}
