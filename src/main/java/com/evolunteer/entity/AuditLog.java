package com.evolunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@TableName(value = "audit_log")
@Data
public class AuditLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer auditNum;

    private String auditOperator;

    private String auditRole;

    private String auditAction;

    private String auditTarget;

    private String auditDetail;

    private String auditIp;

    private Date auditTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
