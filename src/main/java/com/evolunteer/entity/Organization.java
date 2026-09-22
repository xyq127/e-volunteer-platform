package com.evolunteer.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿者组织表 organization，保存志愿者组织的账号、名称、成立日期与组织简介，登录密码统一保存在 e_user 表。
 */
@TableName(value ="organization")
@Data
public class Organization implements Serializable {
    /** 组织编号 */
    @TableId(type = IdType.AUTO)
    private Integer organizationNum;

    /** 组织登录账号 */
    private String organizationId;

    /** 组织名称 */
    private String organizationName;

    /** 成立日期 */
    private Date organizationEstablishdate;

    /** 组织简介 */
    private String organizationIntroduction;

    /** 删除标记 */
    @TableLogic
    private Integer organizationIsdeleted;


    /** 登录账号状态，1 启用、0 停用，平台管理员查询组织名单时关联展示 */
    @TableField(exist = false)
    private Integer accountEnabled;

    /** 组织已申报的志愿活动数量，平台管理员查询组织名单时关联展示 */
    @TableField(exist = false)
    private Integer activityCount;
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
