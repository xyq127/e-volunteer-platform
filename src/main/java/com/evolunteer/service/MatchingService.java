package com.evolunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evolunteer.entity.ActivityMatch;
import com.evolunteer.utils.MatchCalculator;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 供需匹配业务接口：按技能、距离、时间冲突与历史参与四个维度计算志愿者与志愿活动的匹配度，
 * 为志愿者推荐合适的活动，并为志愿者组织审核报名提供匹配参考。
 */
public interface MatchingService {

    /**
     * 分页查询志愿者可报名的志愿活动，并附加匹配度与匹配理由，按匹配度降序排列
     *
     * @param volunteerNum 志愿者编号
     * @param keyword      活动名称或活动地点关键字，可为空
     * @param pageNum      页码
     * @param pageSize     每页条数
     * @return 匹配活动分页结果
     */
    IPage<ActivityMatch> pageRecommendActivities(Integer volunteerNum, String keyword,
                                                 Integer pageNum, Integer pageSize);

    /**
     * 计算指定活动对指定志愿者的匹配度
     *
     * @param volunteerNum 志愿者编号
     * @param activityNum  活动编号
     * @return 匹配度计算结果，活动不存在时返回 null
     */
    MatchCalculator.MatchResult matchForActivity(Integer volunteerNum, Integer activityNum);
}
