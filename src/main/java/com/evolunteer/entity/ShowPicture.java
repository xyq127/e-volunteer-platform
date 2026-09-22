package com.evolunteer.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import lombok.Data;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿秀图片表 show_picture，保存志愿秀内容对应的图片文件信息。
 */
@TableName(value ="show_picture")
@Data
public class ShowPicture implements Serializable {
    /** 图片编号 */
    @TableId(type = IdType.AUTO)
    private Integer pictureNum;

    /** 志愿秀编号 */
    private Integer showNum;

    /** 图片业务编号 */
    private String pictureId;

    /** 图片名称 */
    private String pictureName;

    /** 图片存储路径 */
    private String pictureRoute;

    /** 唯一文件名 */
    private String pictureUniquename;

    /** 删除标记 */
    @TableLogic
    private Integer pictureIsdeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}