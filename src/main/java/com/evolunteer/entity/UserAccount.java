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
 * 统一用户表 e_user，保存志愿者组织与平台管理员的登录账号、密码密文与角色权限。
 */
@TableName(value ="e_user")
@Data
public class UserAccount implements Serializable {
    /** 登录账号 */
    private String id;

    /** 密码密文 */
    private String password;

    /** 角色权限 */
    private String role;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}