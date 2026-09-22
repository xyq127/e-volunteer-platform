package com.evolunteer.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import lombok.Data;

@TableName(value ="policy_announcement")
@Data
public class PolicyAnnouncement implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer policyannouncementNum;

    private String policyannouncementId;

    private String policyannouncementName;

    private String policyannouncementDetail;

    private String policyannouncementFile;

    private Integer adminNum;

    @TableLogic
    private Integer policyannouncementIsdeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
