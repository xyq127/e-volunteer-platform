package com.evolunteer.service;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 数据导出业务接口：面向平台管理员提供志愿者名册与活动清单的 CSV 导出能力，
 * 导出内容带 UTF-8 字节序标记，可直接使用表格软件打开。
 */
public interface ExportService {

    /**
     * 导出志愿者名册
     *
     * @param keyword 志愿者编号、姓名或联系电话关键字，可为空
     * @return 带 UTF-8 字节序标记的 CSV 文本
     */
    String exportVolunteers(String keyword);

    /**
     * 导出活动清单，含申报组织名称与已通过报名人数
     *
     * @param keyword 活动名称或活动编号关键字，可为空
     * @return 带 UTF-8 字节序标记的 CSV 文本
     */
    String exportActivities(String keyword);
}
