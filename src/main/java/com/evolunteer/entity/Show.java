package com.evolunteer.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿秀表 show，保存志愿者分享的志愿服务经历及其浏览、点赞数据。
 */
@TableName(value ="show")
@Data
public class Show implements Serializable {
    /** 志愿秀编号 */
    @TableId(type = IdType.AUTO)
    private Integer showNum;

    /** 志愿秀业务编号 */
    private String showId;

    /** 志愿秀内容 */
    private String showDetail;

    /** 分享时间 */
    private Date showSharetime;

    /** 志愿者编号 */
    private Integer volunteerNum;

    /** 活动编号 */
    private Integer activityNum;

    /** 浏览次数 */
    private Integer showBrowse;

    /** 点赞次数 */
    private Integer showLike;

    /** 删除标记 */
    @TableLogic
    private Integer showIsdeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}