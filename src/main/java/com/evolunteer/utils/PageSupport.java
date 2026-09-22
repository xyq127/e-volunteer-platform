package com.evolunteer.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evolunteer.entity.ApiResponse;

public final class PageSupport {

    private static final int DEFAULT_PAGE = 1;

    private static final int DEFAULT_SIZE = 10;

    private static final int MAX_SIZE = 100;

    private PageSupport() {
    }

    public static <T> Page<T> of(Integer pageNum, Integer pageSize) {
        int current = pageNum == null || pageNum < 1 ? DEFAULT_PAGE : pageNum;
        int size = pageSize == null || pageSize < 1 ? DEFAULT_SIZE : Math.min(pageSize, MAX_SIZE);
        return new Page<>(current, size);
    }

    public static ApiResponse toResponse(IPage<?> page) {
        return ApiResponse.success()
                .add("list", page.getRecords())
                .add("total", page.getTotal())
                .add("page", page.getCurrent())
                .add("size", page.getSize());
    }

    public static String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        return keyword.trim();
    }
}
