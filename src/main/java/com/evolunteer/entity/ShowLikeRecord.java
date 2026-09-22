package com.evolunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

@TableName(value = "show_like_record")
@Data
public class ShowLikeRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer likeNum;

    private Integer showNum;

    private Integer volunteerNum;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
