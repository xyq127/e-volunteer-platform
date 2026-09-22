package com.evolunteer.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import lombok.Data;

@TableName(value ="policy_file")
@Data
public class PolicyFile implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer policyfileNum;

    private Integer policyannouncementNum;

    private String policyfileId;

    private String policyfileName;

    private String policyfileRoute;

    private String policyfileUniquename;

    @TableLogic
    private String policyfileIsdeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
