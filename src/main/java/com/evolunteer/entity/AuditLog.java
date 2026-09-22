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
 * 操作审计表 audit_log，记录平台管理员与志愿者组织的关键操作，
 * 便于事后追溯活动审核、时长复核、活动结算、账号管理与密码重置等行为。
 */
@TableName(value = "audit_log")
@Data
public class AuditLog implements Serializable {
    /** 审计编号 */
    @TableId(type = IdType.AUTO)
    private Integer auditNum;

    /** 操作账号 */
    private String auditOperator;

    /** 操作角色 */
    private String auditRole;

    /** 操作类型编码 */
    private String auditAction;

    /** 操作对象 */
    private String auditTarget;

    /** 操作详情 */
    private String auditDetail;

    /** 操作来源 IP */
    private String auditIp;

    /** 操作时间 */
    private Date auditTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
