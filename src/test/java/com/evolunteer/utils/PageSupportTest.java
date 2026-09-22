package com.evolunteer.utils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evolunteer.entity.ApiResponse;
import com.evolunteer.entity.AuditLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 分页支持工具类单元测试：校验分页参数边界、上限截断与响应字段。
 */
class PageSupportTest {

    @Test
    @DisplayName("分页参数为空或非法时使用默认值，超过上限时按上限截断")
    void ofShouldNormalizePageParams() {
        assertEquals(1, PageSupport.of(null, null).getCurrent());
        assertEquals(10, PageSupport.of(null, null).getSize());
        assertEquals(1, PageSupport.of(0, 0).getCurrent());
        assertEquals(10, PageSupport.of(-3, -1).getSize());
        assertEquals(3, PageSupport.of(3, 20).getCurrent());
        assertEquals(100, PageSupport.of(1, 9999).getSize());
    }

    @Test
    @DisplayName("分页结果按固定字段装入统一响应")
    void toResponseShouldFillStandardFields() {
        Page<AuditLog> page = new Page<>(2, 10, 25);
        page.setRecords(Arrays.asList(new AuditLog(), new AuditLog()));

        ApiResponse response = PageSupport.toResponse(page);
        Map<String, Object> extend = response.getExtend();

        assertEquals(100, response.getCode());
        assertEquals(2, extend.get("list") instanceof java.util.List ? ((java.util.List<?>) extend.get("list")).size() : -1);
        assertEquals(25L, extend.get("total"));
        assertEquals(2L, extend.get("page"));
        assertEquals(10L, extend.get("size"));
    }

    @Test
    @DisplayName("检索关键字去空格，空串统一为 null")
    void normalizeKeywordShouldTrimAndBlankToNull() {
        assertEquals("助学", PageSupport.normalizeKeyword("  助学 "));
        assertNull(PageSupport.normalizeKeyword(""));
        assertNull(PageSupport.normalizeKeyword("   "));
        assertNull(PageSupport.normalizeKeyword(null));
    }
}
