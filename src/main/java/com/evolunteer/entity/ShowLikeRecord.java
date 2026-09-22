package com.evolunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿秀点赞记录表 show_like_record，记录志愿者对志愿秀的点赞，
 * 同一志愿者对同一志愿秀只保留一条记录，保证点赞次数不被重复累加。
 */
@TableName(value = "show_like_record")
@Data
public class ShowLikeRecord implements Serializable {
    /** 点赞编号 */
    @TableId(type = IdType.AUTO)
    private Integer likeNum;

    /** 志愿秀编号 */
    private Integer showNum;

    /** 点赞志愿者编号 */
    private Integer volunteerNum;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
