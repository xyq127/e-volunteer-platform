package com.evolunteer.service;

import com.evolunteer.entity.ActivityMatch;
import com.evolunteer.utils.MatchCalculator;

import java.util.List;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 供需匹配业务接口：按技能、距离、时间冲突与历史参与四个维度计算志愿者与志愿活动的匹配度，
 * 为志愿者推荐合适的活动，并为志愿者组织审核报名提供匹配参考。
 */
public interface MatchingService {

    /**
     * 查询志愿者可报名的志愿活动，并附加匹配度与匹配理由，按匹配度降序排列
     *
     * @param volunteerNum 志愿者编号
     * @return 匹配活动列表
     */
    List<ActivityMatch> recommendActivities(Integer volunteerNum);

    /**
     * 计算指定活动对指定志愿者的匹配度
     *
     * @param volunteerNum 志愿者编号
     * @param activityNum  活动编号
     * @return 匹配度计算结果，活动不存在时返回 null
     */
    MatchCalculator.MatchResult matchForActivity(Integer volunteerNum, Integer activityNum);
}
