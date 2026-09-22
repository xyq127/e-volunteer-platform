package com.evolunteer.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@TableName(value ="training")
@Data
public class Training implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer trainingNum;

    private String trainingId;

    private Integer activityNum;

    private String trainingName;

    private String trainingDetail;

    private Date trainingBegintime;

    private Date trainingEndtime;

    private String trainingLocation;

    private String trainingCheckin;

    @TableLogic
    private Integer trainingIsdeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
