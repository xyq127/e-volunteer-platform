package com.evolunteer.service;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿者组织数据导出业务接口：把活动的报名考勤名单与服务时长明细导出为 CSV 文本，
 * 便于志愿者组织用表格软件汇总归档，替代手工整理。
 */
public interface OrgExportService {

    /**
     * 导出活动报名考勤名单
     *
     * @param activityNum 活动编号
     * @return CSV 文本（含 UTF-8 字节序标记）
     */
    String exportParticipants(Integer activityNum);

    /**
     * 导出活动服务时长明细
     *
     * @param activityNum 活动编号
     * @return CSV 文本（含 UTF-8 字节序标记）
     */
    String exportServiceHours(Integer activityNum);
}
