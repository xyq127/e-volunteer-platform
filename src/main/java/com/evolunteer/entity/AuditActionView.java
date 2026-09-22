package com.evolunteer.entity;

import lombok.Data;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 审计操作类型视图对象：向操作审计查询页面提供操作类型下拉选项，
 * 只包含操作类型编码与中文名称两个字段，不属于任何数据表。
 */
@Data
public class AuditActionView {

    /** 操作类型编码 */
    private String code;

    /** 操作类型名称 */
    private String name;

    /**
     * 构造操作类型下拉选项
     *
     * @param code 操作类型编码
     * @param name 操作类型名称
     */
    public AuditActionView(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
