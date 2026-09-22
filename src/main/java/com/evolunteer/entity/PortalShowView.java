package com.evolunteer.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿秀展示视图：在志愿秀内容的基础上补充分享志愿者姓名、关联活动名称、图片访问路径
 * 与当前访问志愿者是否已点赞，供公众门户志愿秀广场与平台管理员志愿秀管理共用。
 */
@Data
public class PortalShowView implements Serializable {

    /** 志愿秀编号 */
    private Integer showNum;

    /** 志愿秀业务编号 */
    private String showId;

    /** 志愿秀内容 */
    private String showDetail;

    /** 分享时间 */
    private Date showSharetime;

    /** 分享志愿者编号 */
    private Integer volunteerNum;

    /** 分享志愿者姓名 */
    private String volunteerName;

    /** 关联活动编号 */
    private Integer activityNum;

    /** 关联活动名称 */
    private String activityName;

    /** 浏览次数 */
    private Integer showBrowse;

    /** 图片数量，志愿秀未删除图片的条数 */
    private Integer pictureCount;

    /** 点赞次数 */
    private Integer showLike;

    /** 首张图片访问路径，志愿秀未上传图片时为空 */
    private String pictureRoute;

    /** 全部图片访问路径，查询志愿秀详情时按图片编号顺序装配 */
    private List<String> pictureRoutes;

    /** 当前访问志愿者是否已点赞，未登录或未点赞时为 false */
    private Boolean liked;

    private static final long serialVersionUID = 1L;
}
