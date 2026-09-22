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
 * 志愿者技能表 volunteer_skill，保存志愿者登记的服务技能标签，与活动技能要求求交后用于供需匹配打分。
 */
@TableName(value ="volunteer_skill")
@Data
public class VolunteerSkill implements Serializable {
    /** 技能编号 */
    @TableId(type = IdType.AUTO)
    private Integer skillNum;

    /** 志愿者编号 */
    private Integer volunteerNum;

    /** 服务技能标签 */
    private String skillName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
