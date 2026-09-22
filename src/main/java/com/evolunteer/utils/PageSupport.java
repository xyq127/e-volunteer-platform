package com.evolunteer.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evolunteer.entity.ApiResponse;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 分页支持工具类：统一解析页面提交的分页参数，并把分页结果按固定字段装入统一响应结果，
 * 列表接口统一返回 list（当前页数据）、total（总条数）、page（当前页码）、size（每页条数）。
 */
public final class PageSupport {

    /**
     * 默认页码
     */
    private static final int DEFAULT_PAGE = 1;

    /**
     * 默认每页条数
     */
    private static final int DEFAULT_SIZE = 10;

    /**
     * 每页条数上限，避免一次请求拉取过多数据
     */
    private static final int MAX_SIZE = 100;

    private PageSupport() {
    }

    /**
     * 构造分页对象
     *
     * @param pageNum  页码，为空或小于 1 时取默认值
     * @param pageSize 每页条数，为空或非法时取默认值，超过上限时按上限截断
     * @return 分页对象
     */
    public static <T> Page<T> of(Integer pageNum, Integer pageSize) {
        int current = pageNum == null || pageNum < 1 ? DEFAULT_PAGE : pageNum;
        int size = pageSize == null || pageSize < 1 ? DEFAULT_SIZE : Math.min(pageSize, MAX_SIZE);
        return new Page<>(current, size);
    }

    /**
     * 把分页查询结果装入统一响应结果
     *
     * @param page 分页查询结果
     * @return 含 list、total、page、size 的响应结果
     */
    public static ApiResponse toResponse(IPage<?> page) {
        return ApiResponse.success()
                .add("list", page.getRecords())
                .add("total", page.getTotal())
                .add("page", page.getCurrent())
                .add("size", page.getSize());
    }

    /**
     * 关键字模糊查询参数整理：去空格，空串统一为 null，便于映射文件按需拼接模糊条件
     *
     * @param keyword 页面提交的关键字
     * @return 整理后的关键字，无有效关键字时返回 null
     */
    public static String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        return keyword.trim();
    }
}
