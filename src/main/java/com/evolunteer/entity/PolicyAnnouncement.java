package com.evolunteer.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import lombok.Data;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 政策公告表 policy_announcement，保存平台管理员发布的通知公告。
 */
@TableName(value ="policy_announcement")
@Data
public class PolicyAnnouncement implements Serializable {
    /** 公告编号 */
    @TableId(type = IdType.AUTO)
    private Integer policyannouncementNum;

    /** 公告业务编号 */
    private String policyannouncementId;

    /** 公告标题 */
    private String policyannouncementName;

    /** 公告内容 */
    private String policyannouncementDetail;

    /** 公告附件 */
    private String policyannouncementFile;

    /** 管理员编号 */
    private Integer adminNum;

    /** 删除标记 */
    @TableLogic
    private Integer policyannouncementIsdeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}