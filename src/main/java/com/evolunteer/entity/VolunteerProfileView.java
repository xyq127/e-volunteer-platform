package com.evolunteer.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿者档案视图对象：志愿者工作台展示本人的基本信息、服务技能标签、常住地点、
 * 志愿服务信用分与累计服务时长，并给出当前星级与成长进度，不属于任何数据表。
 */
@Data
public class VolunteerProfileView implements Serializable {

    /** 志愿者业务编号 */
    private String volunteerId;

    /** 姓名 */
    private String volunteerName;

    /** 联系电话 */
    private String volunteerTel;

    /** 性别 */
    private String volunteerGender;

    /** 出生日期 */
    private Date volunteerBirth;

    /** 志愿服务信用分 */
    private Integer volunteerCredit;

    /** 活动爽约次数 */
    private Integer volunteerNoshow;

    /** 已核定累计服务时长（小时） */
    private Double volunteerTotalDuration;

    /** 常住地点纬度 */
    private BigDecimal latitude;

    /** 常住地点经度 */
    private BigDecimal longitude;

    /** 服务技能标签 */
    private List<String> skills;

    /** 当前星级 */
    private String starLevel;

    /** 下一星级名称，已达最高星级时为空 */
    private String nextLevelName;

    /** 下一星级所需的累计服务时长（小时），已达最高星级时为空 */
    private Double nextLevelHours;

    /** 距离下一星级还需要的服务时长（小时） */
    private Double hoursToNextLevel;

    private static final long serialVersionUID = 1L;
}
