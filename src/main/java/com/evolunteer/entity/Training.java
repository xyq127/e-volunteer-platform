package com.evolunteer.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿培训表 training，保存志愿活动对应的培训安排。
 */
@TableName(value ="training")
@Data
public class Training implements Serializable {
    /** 培训编号 */
    @TableId(type = IdType.AUTO)
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

    /** 删除标记 */
    @TableLogic
    private Integer trainingIsdeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}