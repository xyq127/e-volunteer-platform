package com.evolunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 平台管理员表 admin，保存平台管理员的账号与姓名，登录密码统一保存在 e_user 表。
 */
@TableName(value ="admin")
@Data
public class Admin implements Serializable {
    /** 管理员编号 */
    @TableId(type = IdType.AUTO)
    private Integer adminNum;

    /** 管理员登录账号 */
    private String adminId;

    /** 管理员姓名 */
    private String adminName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
