package com.evolunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿者表 volunteer，保存志愿者的编号、姓名、联系方式、常住地点，
 * 以及志愿服务信用分、已核定累计服务时长与账号删除标记。
 */
@TableName(value ="volunteer")
@Data
public class Volunteer implements Serializable {
    /** 志愿者编号 */
    @TableId(type = IdType.AUTO)
    private Integer volunteerNum;

    /** 志愿者业务编号，同时作为登录账号 */
    private String volunteerId;

    /** 姓名 */
    private String volunteerName;

    /** 注册日期 */
    private Date volunteerRegisterdate;

    /** 性别 */
    private String volunteerGender;

    /** 出生日期 */
    private Date volunteerBirth;

    /** 联系电话 */
    private String volunteerTel;

    /** 常住地点纬度，用于供需匹配 */
    private BigDecimal volunteerLatitude;

    /** 常住地点经度，用于供需匹配 */
    private BigDecimal volunteerLongitude;

    /** 志愿服务信用分，初始 100 */
    private Integer volunteerCredit;

    /** 活动爽约次数 */
    private Integer volunteerNoshow;

    /** 已核定累计服务时长（小时） */
    private Double volunteerTotalduration;

    /** 删除标记，账号被平台管理员停用时置 1 */
    @TableLogic
    private Integer volunteerIsdeleted;

    /** 登录账号状态，1 启用、0 停用，平台管理员查询志愿者名单时关联展示 */
    @TableField(exist = false)
    private Integer accountEnabled;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
