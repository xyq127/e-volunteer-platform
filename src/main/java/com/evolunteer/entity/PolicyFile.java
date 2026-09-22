package com.evolunteer.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import lombok.Data;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 公告附件表 policy_file，保存通知公告对应的附件文件信息。
 */
@TableName(value ="policy_file")
@Data
public class PolicyFile implements Serializable {
    /** 附件编号 */
    @TableId(type = IdType.AUTO)
    private Integer policyfileNum;

    /** 公告编号 */
    private Integer policyannouncementNum;

    /** 文件业务编号 */
    private String policyfileId;

    /** 文件名称 */
    private String policyfileName;

    /** 文件存储路径 */
    private String policyfileRoute;

    /** 唯一文件名 */
    private String policyfileUniquename;

    /** 删除标记 */
    @TableLogic
    private String policyfileIsdeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}