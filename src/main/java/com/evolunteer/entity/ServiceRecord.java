package com.evolunteer.entity;

import java.io.Serializable;
import lombok.Data;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿服务记录视图对象：志愿服务证明中逐条展示的已核定服务记录，不属于任何数据表。
 */
@Data
public class ServiceRecord implements Serializable {

    /** 活动名称 */
    private String activityName;

    /** 活动业务编号 */
    private String activityId;

    /** 服务日期 */
    private String serviceDate;

    /** 已核定服务时长（小时） */
    private Double duration;

    private static final long serialVersionUID = 1L;
}
