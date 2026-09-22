package com.evolunteer.entity;

import java.util.List;
import lombok.Data;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 活动匹配视图对象：在志愿活动信息之上附加技能要求、距离、历史参与等匹配要素，
 * 供业务层计算匹配度并生成推荐理由，不属于任何数据表。
 */
@Data
public class ActivityMatch extends Activity {

    /** 活动所需技能标签，逗号分隔 */
    private String activitySkillNames;

    /** 志愿者常住地点与活动地点的球面距离（米），缺少定位时为空 */
    private Integer distanceMeters;

    /** 志愿者在该活动所属组织已报名的活动数量 */
    private int organizationHistoryCount;

    /** 匹配度，0 至 100 的整数 */
    private int matchScore;

    /** 命中的技能标签 */
    private List<String> matchedSkills;

    /** 匹配理由，逐条用于页面展示 */
    private List<String> reasons;
}
