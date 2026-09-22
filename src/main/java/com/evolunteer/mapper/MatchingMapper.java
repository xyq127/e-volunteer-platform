package com.evolunteer.mapper;

import com.evolunteer.entity.Activity;
import com.evolunteer.entity.ActivityMatch;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 供需匹配数据访问接口：按志愿者查询可报名的志愿活动并附带技能要求、活动距离与该组织历史参与次数，
 * 同时查询志愿者已报名且尚未结束的活动用于判断时间冲突。
 */
@Repository
public interface MatchingMapper {

    /**
     * 查询志愿活动及其匹配要素
     *
     * @param volunteerNum 志愿者编号
     * @param latitude     志愿者常住地点纬度，未设置时传 null
     * @param longitude    志愿者常住地点经度，未设置时传 null
     * @param activityNum  指定活动编号，传 null 时查询全部活动
     * @param openOnly     是否只查询可报名活动（未开始、报名未截止、服务未结束且本人未报名）
     * @return 活动匹配列表
     */
    List<ActivityMatch> selectActivityMatches(@Param("volunteerNum") Integer volunteerNum,
                                              @Param("latitude") Double latitude,
                                              @Param("longitude") Double longitude,
                                              @Param("activityNum") Integer activityNum,
                                              @Param("openOnly") boolean openOnly);

    /**
     * 查询志愿者已报名且尚未结束的活动，用于判断匹配活动的时间冲突
     *
     * @param volunteerNum 志愿者编号
     * @return 已报名且未结束的活动列表
     */
    List<Activity> selectBusyActivities(@Param("volunteerNum") Integer volunteerNum);
}
