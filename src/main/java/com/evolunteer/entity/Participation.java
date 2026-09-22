package com.evolunteer.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@TableName(value ="participate")
@Data
public class Participation implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer participateNum;

    private Integer volunteerNum;

    private Integer activityNum;

    private Date participateApplytime;

    private String participateApplystate;

    private Date participateApplychecktime;

    private String participateTraining;

    private Date participateBegintime;

    private Date participateEndtime;

    private Double participateDuration;

    private String participateTimecheck;

    private Integer participateNoshow;

    private Date participateCancelTime;

    private String participateConfirmstate;

    private Date participateConfirmtime;

    @TableLogic
    private Integer participateIsdeleted;

    @TableField(exist = false)
    private Volunteer volunteer;

    @TableField(exist = false)
    private Integer matchScore;

    @TableField(exist = false)
    private String matchReasons;

    @TableField(exist = false)
    private String participateConfirmstateText;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
