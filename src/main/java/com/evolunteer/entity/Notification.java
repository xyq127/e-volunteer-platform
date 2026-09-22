package com.evolunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 站内通知表 notification，保存平台向志愿者、志愿者组织发送的站内通知，
 * 用于报名、审核、时长复核、活动提醒与结算结果等消息触达。
 */
@TableName(value = "notification")
@Data
public class Notification implements Serializable {
    /** 通知编号 */
    @TableId(type = IdType.AUTO)
    private Integer notificationNum;

    /** 接收账号 */
    private String receiverId;

    /** 接收角色 */
    private String receiverRole;

    /** 通知标题 */
    private String notificationTitle;

    /** 通知内容 */
    private String notificationDetail;

    /** 是否已读，0 未读、1 已读 */
    private Integer notificationRead;

    /** 通知时间 */
    private Date notificationTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
