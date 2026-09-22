package com.evolunteer.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@TableName(value ="organization")
@Data
public class Organization implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer organizationNum;

    private String organizationId;

    private String organizationName;

    private Date organizationEstablishdate;

    private String organizationIntroduction;

    @TableLogic
    private Integer organizationIsdeleted;

    @TableField(exist = false)
    private Integer accountEnabled;

    @TableField(exist = false)
    private Integer activityCount;
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
