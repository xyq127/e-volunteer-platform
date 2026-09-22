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

@TableName(value ="volunteer")
@Data
public class Volunteer implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer volunteerNum;

    private String volunteerId;

    private String volunteerName;

    private Date volunteerRegisterdate;

    private String volunteerGender;

    private Date volunteerBirth;

    private String volunteerTel;

    private BigDecimal volunteerLatitude;

    private BigDecimal volunteerLongitude;

    private Integer volunteerCredit;

    private Integer volunteerNoshow;

    private Double volunteerTotalduration;

    @TableLogic
    private Integer volunteerIsdeleted;

    @TableField(exist = false)
    private Integer accountEnabled;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
