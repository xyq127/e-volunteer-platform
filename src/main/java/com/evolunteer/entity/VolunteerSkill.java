package com.evolunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

@TableName(value ="volunteer_skill")
@Data
public class VolunteerSkill implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer skillNum;

    private Integer volunteerNum;

    private String skillName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
