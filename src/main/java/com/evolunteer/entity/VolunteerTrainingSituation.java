package com.evolunteer.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@TableName(value ="volunteer_training_situation")
@Data
public class VolunteerTrainingSituation implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer vtsituationNum;

    private Integer trainingNum;

    private Integer volunteerNum;

    private String vtsituationId;

    private Date vtsituationBegintime;

    private Date vtsituationEndtime;

    @TableLogic
    private Integer vtsituationIsdeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
