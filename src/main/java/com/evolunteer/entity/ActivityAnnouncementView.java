package com.evolunteer.entity;

import java.io.Serializable;
import lombok.Data;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿活动公告视图对象：志愿者组织与志愿者查询活动公告时，关联展示公告所属活动名称，
 * 不属于任何数据表。
 */
@Data
public class ActivityAnnouncementView implements Serializable {
    /** 公告编号 */
    private Integer actannouncementNum;

    /** 活动编号 */
    private Integer activityNum;

    /** 公告业务编号 */
    private String actannouncementId;

    /** 公告标题 */
    private String actannouncementName;

    /** 公告内容 */
    private String actannouncementDetail;

    /** 发布公告的志愿者组织编号 */
    private Integer organizationNum;

    /** 公告所属活动名称 */
    private String activityName;

    private static final long serialVersionUID = 1L;
}
