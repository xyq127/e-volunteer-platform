package com.evolunteer.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿者培训情况表 volunteer_training_situation，保存志愿者参加志愿培训的情况。
 */
@TableName(value ="volunteer_training_situation")
@Data
public class VolunteerTrainingSituation implements Serializable {
    /** 培训情况编号 */
    @TableId(type = IdType.AUTO)
    private Integer vtsituationNum;

    /** 培训编号 */
    private Integer trainingNum;

    /** 志愿者编号 */
    private Integer volunteerNum;

    /** 记录业务编号 */
    private String vtsituationId;

    /** 培训开始时间 */
    private Date vtsituationBegintime;

    /** 培训结束时间 */
    private Date vtsituationEndtime;

    /** 删除标记 */
    @TableLogic
    private Integer vtsituationIsdeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}