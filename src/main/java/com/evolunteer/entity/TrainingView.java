package com.evolunteer.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿培训视图对象：志愿者组织与志愿者查询培训安排时，关联培训所属活动名称，
 * 并统计该培训已登记的志愿者培训情况数量，不属于任何数据表。
 */
@Data
public class TrainingView implements Serializable {
    /** 培训编号 */
    private Integer trainingNum;

    /** 培训业务编号 */
    private String trainingId;

    /** 活动编号 */
    private Integer activityNum;

    /** 培训名称 */
    private String trainingName;

    /** 培训内容 */
    private String trainingDetail;

    /** 培训开始时间 */
    private Date trainingBegintime;

    /** 培训结束时间 */
    private Date trainingEndtime;

    /** 培训地点 */
    private String trainingLocation;

    /** 签到方式 */
    private String trainingCheckin;

    /** 培训所属活动名称 */
    private String activityName;

    /** 已登记培训情况的志愿者人数 */
    private Integer situationCount;

    private static final long serialVersionUID = 1L;
}
