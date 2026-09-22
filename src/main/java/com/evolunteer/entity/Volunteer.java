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
 * 志愿者表 volunteer，保存志愿者的编号、姓名、性别、联系方式与注册日期。
 */
@TableName(value ="volunteer")
@Data
public class Volunteer implements Serializable {
    /** 志愿者编号 */
    @TableId(type = IdType.AUTO)
    private Integer volunteerNum;

    /** 志愿者编号 */
    private String volunteerId;

    /** 登录密码密文 */
    private String volunteerPassword;

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


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}