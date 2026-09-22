package com.evolunteer.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import lombok.Data;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿活动公告表 act_announcement，保存志愿者组织针对志愿活动发布的公告。
 */
@TableName(value ="act_announcement")
@Data
public class ActAnnouncement implements Serializable {
    /** 公告编号 */
    @TableId(type = IdType.AUTO)
    private Integer actannouncementNum;

    /** 活动编号 */
    private Integer activityNum;

    /** 公告业务编号 */
    private String actannouncementId;

    /** 公告标题 */
    private String actannouncementName;

    /** 公告内容 */
    private String actannouncementDetail;

    /** 组织编号 */
    private Integer organizationNum;

    /** 删除标记 */
    @TableLogic
    private Integer actannouncementIsdeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}