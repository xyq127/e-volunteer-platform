package com.evolunteer.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿者组织表 organization，保存志愿者组织的账号、名称、成立日期与组织简介。
 */
@TableName(value ="organization")
@Data
public class Organization implements Serializable {
    /** 组织编号 */
    @TableId(type = IdType.AUTO)
    private Integer organizationNum;

    /** 组织登录账号 */
    private String organizationId;

    /** 登录密码密文 */
    private String organizationPassword;

    /** 组织名称 */
    private String organizationName;

    /** 成立日期 */
    private Date organizationEstablishdate;

    /** 组织简介 */
    private String organizationIntroduction;

    /** 删除标记 */
    @TableLogic
    private Integer organizationIsdeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}