package com.evolunteer.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@TableName(value ="show")
@Data
public class VolunteerShow implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer showNum;

    private String showId;

    private String showDetail;

    private Date showSharetime;

    private Integer volunteerNum;

    private Integer activityNum;

    private Integer showBrowse;

    private Integer showLike;

    @TableLogic
    private Integer showIsdeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
